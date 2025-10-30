package org.hmeadow.fittonia.androidServer

import LogType
import Server
import ServerCommandFlag
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.compose.ui.util.fastForEach
import androidx.core.app.ServiceCompat
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.hmeadow.fittonia.AppLogs
import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.hmeadowSocket.AESCipher
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocket
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer
import org.hmeadow.fittonia.mainActivity.MainActivity
import org.hmeadow.fittonia.mainActivity.dataStore
import org.hmeadow.fittonia.models.IncomingJob
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.models.toCompletedJob
import org.hmeadow.fittonia.utility.createJobDirectory
import org.hmeadow.fittonia.utility.debug
import org.hmeadow.fittonia.utility.decodeIpAddress
import org.hmeadow.fittonia.utility.subDivide
import org.hmeadow.fittonia.utility.toString
import org.hmeadow.fittonia.utility.tryOrNull
import org.hmeadow.fittonia.utility.verifyIPAddress
import recordThrowable
import java.io.BufferedInputStream
import java.io.File
import java.io.OutputStream
import java.net.BindException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.coroutines.CoroutineContext

internal class AndroidServer : Service(), CoroutineScope, Server {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        recordThrowable(throwable = throwable)
        debug {
            AppLogs.logError("AndroidServer error: ${throwable.message}")
        }
    }
    override var jobId: Int = 100
    override val jobIdMutex = Mutex()
    private val binder = AndroidServerBinder()
    var serverSocket: ServerSocket? = null
    var serverJob: Job? = null
    private lateinit var accessCode: String
    private val progressUpdateMutex = Mutex()
    private val notificationManagerMutex = Mutex()

    inner class AndroidServerBinder : Binder() {
        fun getService(): AndroidServer = this@AndroidServer
    }

    var transferJobs = MutableStateFlow<List<TransferJob>>(emptyList())
        private set
    private val transferJobsMutex = Mutex()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        AppLogs.logDebug("Server created.")
        super.onCreate()
        server.value = this
    }

    private fun initServerFromIntent(intent: Intent?): Boolean {
        AppLogs.logDebug("initServerFromIntent (intent = $intent)")
        AppLogs.logBlock("Checking for Intent...", type = LogType.DEBUG) { intent != null }
        intent?.let {
            updateAccessCode(
                newAccessCode = it.getStringExtra("org.hmeadow.fittonia.accesscode")
                    ?: throw IllegalStateException("No access code provided"),
            )
            AppLogs.logDebug("Loaded access code.")
            it.getIntExtra("org.hmeadow.fittonia.port", 0).let { port ->
                AppLogs.logDebug("Loaded port $port.")
                if (!AppLogs.logBlock(
                        log = "Starting server socket...",
                        type = LogType.DEBUG,
                    ) { startServerSocket(port = port) }
                ) {
                    return false
                }
            }
            return true
        }
        return true
    }

    fun updateAccessCode(newAccessCode: String) {
        accessCode = newAccessCode
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            constructNotification(transferJobsActive = transferJobs.value.size),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        if (serverSocket == null) {
            if (initServerFromIntent(intent = intent)) {
                launchServerJob()
                return START_STICKY // If the service is killed, it will be automatically restarted.
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            MainActivity.mainActivityForServer?.unbindFromServer()
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY // If the service is killed, it will be automatically restarted.
    }

    private fun startServerSocket(port: Int): Boolean {
        if (port == 0) throw IllegalStateException("No port provided")
        try {
            serverSocket?.close() // Just in case.
            serverSocket = ServerSocket()
            serverSocket?.reuseAddress = true
            serverSocket?.bind(InetSocketAddress(port))
        } catch (e: BindException) {
            serverSocket = null
            AppLogs.logError("Error starting server socket: " + e.message)
            if (e.message?.contains(other = "Address already in use") == true) {
                MainActivity.mainActivityForServer?.alert(UserAlert.PortInUse(port = port))
                return false
            } else {
                throw e
            }
        }
        return true
    }

    private suspend fun handleCommand2(
        jobId: Int,
        server: HMeadowSocketServer,
        theirPublicKey: PuPrKeyCipher.HMPublicKey,
        onPing: suspend (PuPrKeyCipher.HMPublicKey, HMeadowSocketServer, Int) -> Unit,
        onAddDestination: suspend (PuPrKeyCipher.HMPublicKey, HMeadowSocketServer, Int) -> Unit,
        onSendFilesCommand: suspend (PuPrKeyCipher.HMPublicKey, HMeadowSocketServer, Int) -> Unit,
        onSendMessageCommand: suspend (PuPrKeyCipher.HMPublicKey, HMeadowSocketServer, Int) -> Unit,
        onInvalidCommand: suspend (String) -> Unit,
    ) {
        val serverCommand = PuPrKeyCipher.decrypt(server.receiveByteArray()).toString
        when (serverCommand) {
            ServerCommandFlag.PING.text -> onPing
            ServerCommandFlag.SEND_FILES.text -> onSendFilesCommand
            ServerCommandFlag.SEND_MESSAGE.text -> onSendMessageCommand
            ServerCommandFlag.ADD_DESTINATION.text -> onAddDestination
            else -> {
                onInvalidCommand(serverCommand)
                null
            }
        }?.invoke(theirPublicKey, server, jobId)
    }

    private fun launchServerJob() {
        try {
            serverSocket?.let { server ->
                serverJob = launch {
                    while (isActive) {
                        yield()
                        try {
                            AppLogs.log("Server waiting for client connection...")
                            HMeadowSocketServer.createServerFromSocket(server).let { server ->
                                launch {
                                    AppLogs.log("Client attempting to connect.")
                                    val theirPublicKey = server.serverSharePublicKeys(jobId = jobId)
                                    handleCommand2(
                                        jobId = jobId,
                                        server = server,
                                        onPing = ::onPing2,
                                        theirPublicKey = theirPublicKey,
                                        onAddDestination = ::onAddDestination2,
                                        onSendFilesCommand = ::onSendFiles2,
                                        onSendMessageCommand = ::onSendMessage2,
                                        onInvalidCommand = ::onInvalidCommand,
                                    )
                                }
                            }
                        } catch (e: SocketException) {
                            AppLogs.logDebug(e.message ?: "Unknown server SocketException")
                            // TODO: Don't worry! - After release
                        } catch (e: HMeadowSocket.HMeadowSocketError) {
                            AppLogs.logDebug(e.message ?: "HMeadowSocket.HMeadowSocketError")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            // TODO - After release
        }
    }

    fun restartServerSocket(port: Int) {
        debug(
            releaseBlock = { AppLogs.log("Restarting Server") },
            debugBlock = { AppLogs.logDebug("Restarting Server on port $port") },
        )
        serverJob?.cancel()
        serverSocket?.close()
        startServerSocket(port = port)
        launchServerJob()
    }

    private fun constructNotification(transferJobsActive: Int) = Notification
        .Builder(this, getString(R.string.send_receive_channel_id))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOnlyAlertOnce(true)
        .setContentText(
            MainActivity.mainActivityForServer?.resources?.getQuantityString(
                // TODO - after release
                R.plurals.send_receive_foreground_service_notification_content,
                transferJobsActive,
                transferJobsActive,
            ),
        )
        .build()

    private suspend fun updateNotification() {
        val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationManagerMutex.withLock {
            notificationManager.notify(
                NOTIFICATION_ID,
                constructNotification(
                    transferJobsActive = transferJobs.value.filter {
                        it.status == TransferStatus.Sending || it.status == TransferStatus.Receiving
                    }.size,
                ),
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogs.logDebug("onDestroy")
        serverSocket?.close()
        serverJob?.cancel()
        server.value = null
    }

    /* MUST BE IN MUTEX */
    private inline fun <reified T : TransferJob> updateTransferJob(job: T): T {
        transferJobs.update { (transferJobs.value.filterNot { it.id == job.id } + job).sortedBy { it.id } }
        return job
    }

    private inline fun <reified T : TransferJob> findJob(job: T): T? = transferJobs.value.find { it.id == job.id } as? T

    suspend fun registerTransferJob(job: TransferJob) {
        transferJobsMutex.withLock {
            updateTransferJob(job = job)
        }
    }

    private suspend fun updateTransferJobCurrentItem(job: OutgoingJob) {
        transferJobsMutex.withLock {
            findJob(job)?.let { job ->
                updateTransferJob(job.copy(currentItem = job.nextItem))
            }
        }
    }

    private suspend fun finishTransferJob(job: OutgoingJob) {
        transferJobsMutex.withLock {
            findJob(job)?.let { job ->
                updateTransferJob(
                    job.copy(
                        currentItem = job.totalItems,
                        status = TransferStatus.Done,
                    ),
                )
            }
        }
    }

    override fun HMeadowSocketServer.accessCodeIsValid(): Boolean {
        return accessCode == receiveString()
    }

    suspend fun onPing2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        val clientData: PingClientData = AppLogs.logBlockResult(
            log = "Server waiting for PingClientData.",
            type = LogType.DEBUG,
        ) {
            server.receiveAndDecrypt<PingClientData>()
        }
        server.encryptAndSend(
            data = PingServerData(isAccessCodeCorrect = clientData.accessCode == accessCode),
            theirPublicKey = theirPublicKey,
        )
    }

    suspend fun onAddDestination2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        // println("onAddDestination2()") // todo is this needed?
    }

    suspend fun onSendFiles2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        var currentJob = IncomingJob(id = jobId)
        registerTransferJob(currentJob)
        val clientData: SendFileClientData = AppLogs.logBlockResult(
            log = "Receiving client data.",
            type = LogType.NORMAL,
        ) {
            server.receiveAndDecrypt<SendFileClientData>()
        }
        if (clientData.accessCode != accessCode) {
            AppLogs.logError(log = "Client sent incorrect accesscode.")
            return
        }
        currentJob = updateTransferJob(job = currentJob.copy(items = clientData.items, currentItem = 1))
        val newJobDirectory: MainActivity.CreateDumpDirectory = AppLogs.logBlockResultS(
            log = "Creating incoming data folder",
            type = LogType.NORMAL,
        ) {
            createJobDirectory(jobName = clientData.jobName)
        }

        if (newJobDirectory is MainActivity.CreateDumpDirectory.Success) {
            val serverData: SendFileServerData = if (clientData.accessCode == accessCode) {
                SendFileServerData(
                    jobName = newJobDirectory.name,
                    pathLimit = 128,
                    isAccessCodeCorrect = true,
                )
            } else {
                SendFileServerData(
                    jobName = "",
                    pathLimit = 0,
                    isAccessCodeCorrect = false,
                )
            }
            AppLogs.logBlockResultS(log = "Sending server data...", type = LogType.NORMAL) {
                server.encryptAndSend(data = serverData, theirPublicKey = theirPublicKey)
            }
            currentJob = updateTransferJob(job = currentJob.copy(description = newJobDirectory.name))
            AppLogs.logDebug(log = "New job path: ${newJobDirectory.uri}", jobId = jobId)
            val decryptionFileCache: File = createTempFile()
            currentJob.cloneItems().fastForEach { item ->
                if (item.isFile) {
                    AppLogs.logBlock("Receiving encrypted file...") {
                        decryptionFileCache.outputStream().use { output ->
                            server.receiveFile(
                                onOutputStream = { output },
                                progressPrecision = 0.01,
                                onProgressUpdate = { progress ->
                                    currentJob = safelyUpdateJobItem(
                                        job = currentJob,
                                        item = item,
                                        itemBytes = item.progressBytes + progress,
                                    )
                                },
                            )
                            currentJob = safelyUpdateJobItem(job = currentJob, item = item, itemBytes = item.sizeBytes)
                        }
                        true
                    }
                    AppLogs.logBlock("Decrypting file...") {
                        val decryptedFile: OutputStream? = getUriOutputStream(
                            uri = createFile(
                                path = newJobDirectory.uri,
                                fileName = item.name,
                            )!!,
                        )
                        val encryptedSize = decryptionFileCache.length()
                        BufferedInputStream(decryptionFileCache.inputStream()).use {
                            it.subDivide(
                                maxBlockSize = ENCRYPTED_AES_BLOCK_SIZE,
                                expectedStreamSize = encryptedSize,
                                block = { bytes ->
                                    decryptedFile?.write(
                                        AESCipher.decryptBytes(
                                            bytes = bytes,
                                            secretKeyBytes = clientData.aesKey,
                                        ),
                                    )
                                },
                            )
                        }
                        decryptedFile?.close()
                        true
                    }
                    decryptionFileCache.delete()
                    currentJob = updateTransferJob(job = currentJob.copy(currentItem = currentJob.nextItem))
                    server.sendContinue()
                }
            }
            currentJob = updateTransferJob(currentJob.copy(status = TransferStatus.Done))
            saveCompletedJob(job = currentJob)
            AppLogs.log(log = "Transfer job complete.")
        } else {
            updateTransferJob(job = currentJob.copy(status = TransferStatus.Error))
        }
    }

    suspend fun onSendMessage2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        // println("onSendMessage2()") // todo
    }

    override suspend fun onPing(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientAccessCodeSuccess) {
            AppLogs.logWarning("Client attempted to ping this server, access code refused.", jobId = jobId)
        } else {
            AppLogs.logWarning("Client successfully pinged this server.", jobId = jobId)
        }
    }

    override suspend fun onAddDestination(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientAccessCodeSuccess) {
            AppLogs.logWarning(
                "Client attempted to add this server as destination, access code refused.",
                jobId = jobId,
            )
        } else {
            if (server.receiveBoolean()) {
                AppLogs.log("Client added this server as a destination.", jobId = jobId)
            } else {
                AppLogs.logWarning("Client failed to add this server as a destination.", jobId = jobId)
            }
        }
    }

    override suspend fun onSendFiles(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        // TODO - after release - Obsolete.
    }

    override suspend fun onSendMessage(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientAccessCodeSuccess) {
            AppLogs.logWarning("Client attempted to send a message, access code refused.", jobId = jobId)
        } else {
            AppLogs.log("Client message: ${server.receiveString()}", jobId = jobId)
            server.sendConfirmation()
        }
    }

    override suspend fun onInvalidCommand(unknownCommand: String) {
        AppLogs.logWarning("Received invalid server command from client: $unknownCommand", jobId = jobId)
    }

    private fun createFile(path: Uri, fileName: String): Uri? {
        return DocumentFile
            .fromTreeUri(this, path)
            ?.createFile("*/*", fileName)
            ?.uri
    }

    private fun getUriInputStream(uri: Uri) = contentResolver.openInputStream(uri)
    private fun getUriOutputStream(uri: Uri) = contentResolver.openOutputStream(uri)

    private suspend fun createTempFile(): File = withContext(Dispatchers.IO) {
        File.createTempFile("fittonia", ".xyz", cacheDir)
    }

    private fun safelyUpdateJobItem(job: IncomingJob, item: TransferJob.Item, itemBytes: Long): IncomingJob {
        return runBlocking {
            progressUpdateMutex.withLock {
                job.updateItem(item.copy(progressBytes = itemBytes)).also {
                    updateTransferJob(job = it)
                }
            }
        }
    }

    private fun safelyUpdateJobItem(
        job: OutgoingJob,
        item: TransferJob.Item,
        itemBytes: Long,
        bytesPerSecond: Long,
    ): OutgoingJob {
        return runBlocking {
            progressUpdateMutex.withLock {
                job.updateItem(item.copy(progressBytes = itemBytes)).also {
                    updateTransferJob(job = it.copy(bytesPerSecond = bytesPerSecond))
                }
            }
        }
    }

    private fun saveCompletedJob(job: TransferJob) = launch {
        dataStore.updateData {
            it.copy(completedJobs = it.completedJobs + job.toCompletedJob)
        }
    }

    companion object {
        private const val AES_BLOCK_SIZE = 8192 * 32
        private const val ENCRYPTED_AES_BLOCK_SIZE = AES_BLOCK_SIZE + 16
        const val NOTIFICATION_ID = 455

        var socketLogDebug = false
        var server: MutableStateFlow<AndroidServer?> = MutableStateFlow(null)

        private suspend fun bootStrap(block: suspend AndroidServer.() -> Unit) {
            val mainActivity = MainActivity.mainActivityForServer ?: return
            if (server.value == null) {
                mainActivity.attemptStartServer()
                server.first()
            }
            server.first()?.run {
                launch {
                    try {
                        block()
                    } catch (e: HMeadowSocket.HMeadowSocketError) { // TODO Better error handling & messaging. - After R
                        e.hmMessage?.let { AppLogs.logError(it) }
                        e.message?.let { AppLogs.logError("${e.javaClass::class} $it") }
                    } catch (e: Exception) {
                        // TODO before release make this more widespread.
                        e.message?.let { AppLogs.logError("${e.javaClass} - $it") }
                    }
                }
            }
        }

        private suspend fun <T> bootStrap(onError: () -> T, block: suspend AndroidServer.() -> T): T {
            val mainActivity = MainActivity.mainActivityForServer ?: return onError()
            if (server.value == null) {
                mainActivity.attemptStartServer()
            }
            return server.first()?.run {
                async {
                    try {
                        block()
                    } catch (e: HMeadowSocket.HMeadowSocketError) {
                        // TODO Better error handling & messaging. - After release
                        e.cause?.let { recordThrowable(throwable = it) }
                        onError()
                    } catch (e: Exception) {
                        recordThrowable(throwable = e)
                        onError()
                    }
                }.await()
            } ?: onError()
        }

        suspend fun ping(ip: String, port: Int, accessCode: String, requestTimestamp: Long): Ping {
            return Ping(
                pingStatus = bootStrap(onError = { PingStatus.InternalBug }) {
                    val client: HMeadowSocketClient
                    try {
                        client = HMeadowSocketClient(
                            ipAddress = tryOrNull { decodeIpAddress(ipAddress = ip) } ?: ip,
                            port = port,
                            operationTimeoutMillis = 2000,
                            handshakeTimeoutMillis = 2000,
                        )
                    } catch (socketError: SocketTimeoutException) {
                        socketError.message?.let { AppLogs.logError(it) }
                        recordThrowable(throwable = socketError)
                        return@bootStrap PingStatus.CouldNotConnect
                    } catch (e: HMeadowSocket.HMeadowSocketError) {
                        e.cause?.let { recordThrowable(throwable = it) }
                        return@bootStrap PingStatus.InternalBug
                    } catch (e: Exception) {
                        recordThrowable(e)
                        return@bootStrap PingStatus.CouldNotConnect
                    }
                    val theirPublicKey = clientSharePublicKeys(client)
                    client.sendCommandFlag(commandFlag = ServerCommandFlag.PING, theirPublicKey = theirPublicKey)
                    client.encryptAndSend(
                        data = PingClientData(accessCode = accessCode),
                        theirPublicKey = theirPublicKey,
                    )
                    if (client.receiveAndDecrypt<PingServerData>().isAccessCodeCorrect) {
                        PingStatus.Success
                    } else {
                        PingStatus.IncorrectAccessCode
                    }.also {
                        debug {
                            println("Client received PingServerData: $it")
                        }
                    }
                },
                requestTimestamp = requestTimestamp,
            )
        }

        suspend fun startSending(newJob: OutgoingJob) {
            bootStrap {
                var currentJob = newJob.copy(description = newJob.getInitialDescriptionText())
                registerTransferJob(currentJob)
                updateNotification()
                val client: HMeadowSocketClient
                try {
                    client = currentJob.createClient()
                    val theirPublicKey = clientSharePublicKeys(client = client)
                    client.sendCommandFlag(commandFlag = ServerCommandFlag.SEND_FILES, theirPublicKey = theirPublicKey)

                    val aesKey = AESCipher.generateKey()
                    val sendFileClientData = SendFileClientData(
                        items = newJob.items,
                        aesKey = aesKey,
                        jobName = newJob.description.takeUnless { newJob.needDescription },
                        accessCode = currentJob.destination.accessCode,
                    )
                    client.encryptAndSend(
                        data = sendFileClientData,
                        theirPublicKey = theirPublicKey,
                    )
                    val serverData = client.receiveAndDecrypt<SendFileServerData>()
                    if (serverData.isAccessCodeCorrect) {
                        currentJob = currentJob.copy(description = serverData.jobName)
                        updateTransferJob(currentJob)
                        val encryptionFileCache = createTempFile()
                        currentJob.cloneItems().fastForEach { item ->
                            // TODO: Relative path - After release
                            if (item.isFile) {
                                encryptionFileCache.outputStream().use { encryptionStream ->
                                    getUriInputStream(item.uri())?.use { itemInputStream ->
                                        BufferedInputStream(itemInputStream).subDivide(
                                            maxBlockSize = AES_BLOCK_SIZE,
                                            expectedStreamSize = item.sizeBytes,
                                            block = { bytes ->
                                                encryptionStream.write(
                                                    AESCipher.encryptBytes(
                                                        bytes = bytes,
                                                        secretKeyBytes = sendFileClientData.aesKey,
                                                    ),
                                                )
                                            },
                                        )
                                    }
                                }
                                BufferedInputStream(encryptionFileCache.inputStream()).use {
                                    var bytesPerSecondTime: Long = System.nanoTime()
                                    client.sendFile(
                                        stream = it,
                                        name = item.name,
                                        size = encryptionFileCache.length(),
                                        progressPrecision = 0.01,
                                    ) { progressBytes ->
                                        currentJob = safelyUpdateJobItem(
                                            job = currentJob,
                                            item = item,
                                            itemBytes = progressBytes,
                                            bytesPerSecond = (progressBytes / ((System.nanoTime() - bytesPerSecondTime)) / 1_000_000_000),
                                        )
                                    }
                                    currentJob = safelyUpdateJobItem(
                                        job = currentJob,
                                        item = item,
                                        itemBytes = item.sizeBytes,
                                        bytesPerSecond = 0,
                                    )
                                }
                            }
                            client.receiveContinue()
                        }
                        encryptionFileCache.delete()
                        updateTransferJobCurrentItem(job = currentJob) // TODO needed? - After release
                    }
                    finishTransferJob(currentJob)
                    updateNotification()
                } catch (e: HMeadowSocket.HMeadowSocketError) {
                    e.cause?.let { recordThrowable(throwable = it) }
                    return@bootStrap
                }
            }
        }
    }
}

fun OutgoingJob.createClient() = HMeadowSocketClient(
    ipAddress = destination.ip.verifyIPAddress(),
    port = port,
    operationTimeoutMillis = 1000 * 30,
    handshakeTimeoutMillis = 1000 * 5,
)

fun OutgoingJob.getInitialDescriptionText() = if (needDescription) "Connecting..." else description

fun HMeadowSocketClient.sendCommandFlag(commandFlag: ServerCommandFlag, theirPublicKey: PuPrKeyCipher.HMPublicKey) {
    sendByteArray(
        message = PuPrKeyCipher.encrypt(
            byteArray = commandFlag.text.encodeToByteArray(),
            publicKey = theirPublicKey,
        ),
    )
}
