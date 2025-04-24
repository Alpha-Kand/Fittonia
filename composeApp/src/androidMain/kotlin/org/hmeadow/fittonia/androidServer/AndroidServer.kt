package org.hmeadow.fittonia.androidServer

import Log
import Server
import ServerCommandFlag
import ServerLogs
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
import communicateCommandBoolean
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.hmeadowSocket.AESCipher
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocket
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer
import org.hmeadow.fittonia.models.IncomingJob
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.utility.createJobDirectory
import org.hmeadow.fittonia.utility.subDivide
import org.hmeadow.fittonia.utility.toString
import java.io.BufferedInputStream
import java.io.File
import java.net.BindException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.coroutines.CoroutineContext

class AndroidServer : Service(), CoroutineScope, ServerLogs, Server {
    override val mLogs = mutableListOf<Log>()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        println("AndroidServer error: ${throwable.message}") // TODO - handle errors, crashlytics? before release
    }
    override var jobId: Int = 100
    override val jobIdMutex = Mutex()
    override val logsMutex = Mutex()
    private val binder = AndroidServerBinder()
    var serverSocket: ServerSocket? = null
    var serverJob: Job? = null
    private lateinit var password: String
    private val progressUpdateMutex = Mutex()
    private val notificationManagerMutex = Mutex()

    inner class AndroidServerBinder : Binder() {
        fun getService(): AndroidServer = this@AndroidServer
    }

    var transferJobs = MutableStateFlow<List<TransferJob>>(emptyList())
        private set
    private val transferJobsMutex = Mutex()

    override fun onBind(intent: Intent): IBinder {
        serverLog(text = "onBind")
        return binder
    }

    override fun onCreate() {
        serverLog(text = "onCreate")
        super.onCreate()
        server.value = this
    }

    private fun initServerFromIntent(intent: Intent?): Boolean {
        serverLog(text = "initServerFromIntent (intent = $intent)")
        intent?.let {
            password = it.getStringExtra("org.hmeadow.fittonia.password") ?: throw Exception("No password provided")
            serverLog(text = "init password $password") // TODO - before release
            it.getIntExtra("org.hmeadow.fittonia.port", 0).let { port ->
                serverLog(text = "init port $port")
                if (!startServerSocket(port = port)) {
                    return false
                }
            }
            return true
        }
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand")
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            constructNotification(transferJobsActive = transferJobs.value.size),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        if (serverSocket == null) {
            if (initServerFromIntent(intent = intent)) {
                launchServerJob()
                println("START_STICKY")
                return START_STICKY // If the service is killed, it will be automatically restarted.
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            MainActivity.mainActivityForServer?.unbindFromServer()
            stopSelf()
            println("START_NOT_STICKY")
            return START_NOT_STICKY
        }
        return START_STICKY // If the service is killed, it will be automatically restarted.
    }

    private fun startServerSocket(port: Int): Boolean {
        serverLog(text = "startServerSocket")
        if (port == 0) throw Exception("No port provided")
        try {
            serverLog(text = "Starting server on port $port.")
            serverSocket?.close() // Just in case.
            serverSocket = ServerSocket()
            serverSocket?.reuseAddress = true
            serverSocket?.bind(InetSocketAddress(port))
            serverLog(text = "Started server success!")
        } catch (e: BindException) {
            serverSocket = null
            serverLog(text = "e.message: $port " + e.message)
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
                    while (true) {
                        yield()
                        try {
                            HMeadowSocketServer.createServerFromSocket(server).let { server ->
                                launch {
                                    log("Connected to client.")
                                    val theirPublicKey = serverSharePublicKeys(server = server, jobId = jobId)
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

                                    /*
                                    handleCommand(
                                        server = server,
                                        jobId = getAndIncrementJobId(),
                                    )
                                     */
                                }
                            }
                        } catch (e: SocketException) {
                            log(e.message ?: "Unknown server SocketException")
                            // TODO: Don't worry! - After release
                        } catch (e: HMeadowSocket.HMeadowSocketError) {
                            log(e.message ?: "HMeadowSocket.HMeadowSocketError")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            // TODO - After release
        }
    }

    fun restartServerSocket(port: Int) {
        println("Restarting server on port $port")
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
        println("onDestroy")
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

    override fun HMeadowSocketServer.passwordIsValid(): Boolean {
        return password == receiveString()
    }

    suspend fun onPing2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        println("Server waiting for PingClientData")
        val clientData = server.receiveAndDecrypt<PingClientData>()
        println("onPing2.clientData.password: ${clientData.password}")
        server.encryptAndSend(
            data = PingServerData(isPasswordCorrect = clientData.password == password),
            theirPublicKey = theirPublicKey,
        )
    }

    suspend fun onAddDestination2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        println("onAddDestination2()")
    }

    suspend fun onSendFiles2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        println("onSendFiles2()")
        var currentJob = IncomingJob(id = jobId)
        registerTransferJob(currentJob)

        val clientData = server.receiveAndDecrypt<SendFileClientData>()
        if (clientData.password != password) return
        currentJob = updateTransferJob(currentJob.copy(items = clientData.items, currentItem = 1))
        val newJobDirectory = createJobDirectory(
            jobName = clientData.jobName,
            print = { this.logDebug(it, jobId = jobId) },
        )

        if (newJobDirectory is MainActivity.CreateDumpDirectory.Success) {
            val serverData = if (clientData.password == password) {
                SendFileServerData(
                    jobName = newJobDirectory.name,
                    pathLimit = 128,
                    isPasswordCorrect = true,
                )
            } else {
                SendFileServerData(
                    jobName = "",
                    pathLimit = 0,
                    isPasswordCorrect = false,
                )
            }
            server.encryptAndSend(data = serverData, theirPublicKey = theirPublicKey)
            currentJob = updateTransferJob(job = currentJob.copy(description = newJobDirectory.name))
            logDebug("jobPath: ${newJobDirectory.uri}", jobId = jobId)
            val decryptionFileCache = createTempFile()
            currentJob.cloneItems().fastForEach { item ->
                if (item.isFile) { // Is a file...
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
                    val decryptedFile = getUriOutputStream(
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
                    currentJob = updateTransferJob(currentJob.copy(currentItem = currentJob.nextItem))
                    server.sendContinue()
                }
            }
            currentJob = updateTransferJob(currentJob.copy(status = TransferStatus.Done))
        } else {
            updateTransferJob(currentJob.copy(status = TransferStatus.Error))
        }
    }

    suspend fun onSendMessage2(theirPublicKey: PuPrKeyCipher.HMPublicKey, server: HMeadowSocketServer, jobId: Int) {
        println("onSendMessage2()")
    }

    override suspend fun onPing(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to ping this server, password refused.", jobId = jobId)
        } else {
            log("Client successfully pinged this server.", jobId = jobId)
        }
    }

    override suspend fun onAddDestination(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to add this server as destination, password refused.", jobId = jobId)
        } else {
            if (server.receiveBoolean()) {
                log("Client added this server as a destination.", jobId = jobId)
            } else {
                logWarning("Client failed to add this server as a destination.", jobId = jobId)
            }
        }
    }

    override suspend fun onSendFiles(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        // TODO - after release - Obsolete.
    }

    override suspend fun onSendMessage(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to send a message, password refused.", jobId = jobId)
        } else {
            log("Client message: ${server.receiveString()}", jobId = jobId)
            server.sendConfirmation()
        }
    }

    override suspend fun onInvalidCommand(unknownCommand: String) {
        logWarning("Received invalid server command from client: $unknownCommand", jobId = jobId)
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

    private fun safelyUpdateJobItem(job: OutgoingJob, item: TransferJob.Item, itemBytes: Long): OutgoingJob {
        return runBlocking {
            progressUpdateMutex.withLock {
                job.updateItem(item.copy(progressBytes = itemBytes)).also {
                    updateTransferJob(job = it)
                }
            }
        }
    }

    companion object {
        private const val AES_BLOCK_SIZE = 8192 * 32
        private const val ENCRYPTED_AES_BLOCK_SIZE = AES_BLOCK_SIZE + 16
        const val NOTIFICATION_ID = 455

        var socketLogDebug = false
        var server: MutableStateFlow<AndroidServer?> = MutableStateFlow(null)

        fun serverLog(text: String) {
            if (socketLogDebug) {
                println("Server Debug: $text")
            }
        }

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
                        e.hmMessage?.let { logError(it) }
                        e.message?.let { logError(it) }
                    } catch (e: Exception) {
                        // TODO before release make this more widespread.
                        e.message?.let { logError("${e.javaClass} - $it") }
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
                        e.hmMessage?.let { logError(it) }
                        e.message?.let { logError(it) }
                        onError()
                    } catch (e: Exception) {
                        e.message?.let { logError("${e.javaClass} - $it") }
                        onError()
                    }
                }.await()
            } ?: onError()
        }

        suspend fun ping(ip: String, port: Int, password: String, requestTimestamp: Long): Ping {
            return Ping(
                pingStatus = bootStrap(onError = { PingStatus.InternalBug }) {
                    val client: HMeadowSocketClient
                    try {
                        client = HMeadowSocketClient(
                            ipAddress = ip,
                            port = port,
                            operationTimeoutMillis = 2000,
                            handshakeTimeoutMillis = 2000,
                        )
                    } catch (socketError: SocketTimeoutException) {
                        socketError.message?.let { logError(it) }
                        return@bootStrap PingStatus.CouldNotConnect
                    } catch (e: HMeadowSocket.HMeadowSocketError) {
                        e.hmMessage?.let { logError(it) }
                        e.message?.let { logError(it) }
                        return@bootStrap PingStatus.InternalBug
                    }
                    val theirPublicKey = clientSharePublicKeys(client)
                    client.sendCommandFlag(commandFlag = ServerCommandFlag.PING, theirPublicKey = theirPublicKey)
                    client.encryptAndSend(
                        data = PingClientData(password = password),
                        theirPublicKey = theirPublicKey,
                    )
                    println("Client awaiting PingServerData")
                    if (client.receiveAndDecrypt<PingServerData>().isPasswordCorrect) {
                        PingStatus.Success
                    } else {
                        PingStatus.IncorrectPassword
                    }.also {
                        println("Client received PingServerData: $it")
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
                        password = currentJob.destination.password,
                    )
                    client.encryptAndSend(
                        data = sendFileClientData,
                        theirPublicKey = theirPublicKey,
                    )
                    val serverData = client.receiveAndDecrypt<SendFileServerData>()
                    if (serverData.isPasswordCorrect) {
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
                                        )
                                    }
                                    currentJob = safelyUpdateJobItem(
                                        job = currentJob,
                                        item = item,
                                        itemBytes = item.sizeBytes,
                                    )
                                }
                            }
                            client.receiveContinue()
                        }
                        updateTransferJobCurrentItem(job = currentJob) // TODO needed? - After release
                    }
                    finishTransferJob(currentJob)
                    updateNotification()
                } catch (e: HMeadowSocket.HMeadowSocketError) {
                    e.hmMessage?.let { logError(it) }
                    e.message?.let { logError(it) }
                    return@bootStrap
                }
            }
        }
    }
}

fun OutgoingJob.createClient() = HMeadowSocketClient(
    ipAddress = destination.ip,
    port = port,
    operationTimeoutMillis = 1000 * 30,
    handshakeTimeoutMillis = 1000 * 5,
)

fun AndroidServer.communicateCommand(client: HMeadowSocketClient, currentJob: OutgoingJob): Boolean {
    return client.communicateCommandBoolean(
        commandFlag = ServerCommandFlag.SEND_FILES,
        password = currentJob.destination.password,
        onSuccess = { },
        onPasswordRefused = { logError("Server refused password.") },
        onFailure = { logError("Connected, but request refused.") },
    )
}

fun OutgoingJob.getInitialDescriptionText() = if (needDescription) "Connecting..." else description

fun HMeadowSocketClient.sendCommandFlag(commandFlag: ServerCommandFlag, theirPublicKey: PuPrKeyCipher.HMPublicKey) {
    sendByteArray(
        message = PuPrKeyCipher.encrypt(
            byteArray = commandFlag.text.encodeToByteArray(),
            publicKey = theirPublicKey,
        ),
    )
}
