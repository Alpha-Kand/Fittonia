package org.hmeadow.fittonia.androidServer

import Log
import Server
import ServerCommandFlag
import ServerFlagsString
import ServerLogs
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Binder
import android.os.IBinder
import androidx.compose.ui.util.fastForEach
import androidx.core.app.ServiceCompat
import androidx.documentfile.provider.DocumentFile
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import communicateCommand
import communicateCommandBoolean
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
import kotlinx.coroutines.yield
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocket
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer
import org.hmeadow.fittonia.models.IncomingJob
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferJob.Item
import org.hmeadow.fittonia.models.TransferStatus
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.BindException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.random.Random

class AndroidServer : Service(), CoroutineScope, ServerLogs, Server {
    override val mLogs = mutableListOf<Log>()
    override val coroutineContext: CoroutineContext = Dispatchers.IO
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
                                    handleCommand(
                                        server = server,
                                        jobId = getAndIncrementJobId(),
                                    )
                                }
                            }
                        } catch (e: SocketException) {
                            log(e.message ?: "Unknown server SocketException")
                            // TODO: Don't worry! - After release
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
            MainActivity.mainActivity.resources.getQuantityString(
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

    private suspend fun registerTransferJob(job: TransferJob) {
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
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to send files to this server, password refused.", jobId = jobId)
        } else {
            var job = IncomingJob(id = jobId)
            registerTransferJob(job)
            log("Client attempting to send files.", jobId = jobId)
            // TODO ENCRYPT PASSWORDS

            val sendFileClientData: SendFileClientData = String(
                bytes = server.receiveByteArray(),
                charset = StandardCharsets.UTF_8,
            ).let { json ->
                jacksonObjectMapper().readValue<SendFileClientData>(json)
            }
            println("Server received: $sendFileClientData")

            val jobName = when (sendFileClientData.nameFlag) {
                ServerFlagsString.NEED_JOB_NAME -> "Job${abs(Random.nextInt()) % 100000}".also {
                    // TODO eliminate possible conflicts BEFORE RELEASE
                    log("Server generated job name: $it", jobId = jobId)
                }

                ServerFlagsString.HAVE_JOB_NAME -> sendFileClientData.jobName.also {
                    log("Client provided job name: $it", jobId = jobId)
                }

                else -> throw Exception() // TODO - After release
            }

            val sendFileServerData = SendFileServerData(
                jobName = jobName,
                pathLimit = 128, // TODO remove constant - After release
            )
            val byteArrayOutputStream = ByteArrayOutputStream()
            jacksonObjectMapper().writeValue(byteArrayOutputStream, sendFileServerData)
            server.sendByteArray(byteArrayOutputStream.toByteArray()) // TODO ENCRYPT BEFORE RELEASE

            job = updateTransferJob(job.copy(items = sendFileClientData.items, description = jobName, currentItem = 1))

            val aaa = MainActivity.mainActivity.createJobDirectory(
                jobName = jobName,
                print = { this.logDebug(it, jobId = jobId) },
            )
            if (aaa is MainActivity.CreateDumpDirectory.Success) {
                val jobPath = aaa.uri
                logDebug("jobPath: $jobPath", jobId = jobId)
                job.cloneItems().fastForEach { item ->
                    if (server.receiveBoolean()) { // Is a file...
                        server.receiveFile(
                            onOutputStream = { fileName ->
                                DocumentFile
                                    .fromTreeUri(MainActivity.mainActivity, jobPath)
                                    ?.createFile("*/*", fileName)
                                    ?.let { file ->
                                        MainActivity.mainActivity.contentResolver.openOutputStream(file.uri)
                                    }
                            },
                            progressPrecision = 0.01,
                            onProgressUpdate = { progress ->
                                runBlocking {
                                    progressUpdateMutex.withLock {
                                        job = job.updateItem(item.copy(progressBytes = item.progressBytes + progress))
                                        updateTransferJob(job = job)
                                    }
                                }
                            },
                        )
                        job = updateTransferJob(job.copy(currentItem = job.nextItem))
                        server.sendContinue()
                    }
                }
                job = updateTransferJob(job.copy(status = TransferStatus.Done))
            } else {
                updateTransferJob(job.copy(status = TransferStatus.Error))
            }
        }
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

    companion object {
        const val NOTIFICATION_ID = 455

        var socketLogDebug = false
        var server: MutableStateFlow<AndroidServer?> = MutableStateFlow(null)

        fun serverLog(text: String) {
            if (socketLogDebug) {
                println("Server Debug: $text")
            }
        }

        private suspend fun bootStrap(block: suspend AndroidServer.() -> Unit) {
            if (server.value == null) {
                MainActivity.mainActivity.attemptStartServer()
                server.first()
            }
            server.value?.run {
                launch {
                    try {
                        block()
                    } catch (e: HMeadowSocket.HMeadowSocketError) { // TODO Better error handling & messaging. - After R
                        e.hmMessage?.let { logError(it) }
                        e.message?.let { logError(it) }
                    } catch (e: Exception) {
                        e.message?.let { logError(it) }
                    }
                }
            }
        }

        private suspend fun <T> bootStrap(onError: () -> T, block: suspend AndroidServer.() -> T): T {
            if (server.value == null) {
                MainActivity.mainActivity.attemptStartServer()
                server.first()
            }
            return server.value?.run {
                async {
                    try {
                        block()
                    } catch (e: HMeadowSocket.HMeadowSocketError) {
                        // TODO Better error handling & messaging. - After release
                        e.hmMessage?.let { logError(it) }
                        e.message?.let { logError(it) }
                        onError()
                    } catch (e: Exception) {
                        e.message?.let { logError(it) }
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

                    client.communicateCommand(
                        commandFlag = ServerCommandFlag.PING,
                        password = password,
                        onSuccess = { PingStatus.Success },
                        onPasswordRefused = { PingStatus.IncorrectPassword },
                        onFailure = { PingStatus.ConnectionRefused },
                    )
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
                } catch (e: HMeadowSocket.HMeadowSocketError) {
                    e.hmMessage?.let { logError(it) }
                    e.message?.let { logError(it) }
                    return@bootStrap
                }
                // client.sendBytesPerSecond = 2 * 1024 * 1024 // TODO have this come from the screen. - After release
                val commandSuccess = communicateCommand(client = client, currentJob = currentJob)
                if (commandSuccess) {
                    log(log = "Password accepted")

                    val sendFileClientData = SendFileClientData(
                        items = newJob.items,
                        aesKey = ByteArray(0),
                        jobName = newJob.description,
                        nameFlag = if (newJob.needDescription) {
                            ServerFlagsString.NEED_JOB_NAME
                        } else {
                            ServerFlagsString.HAVE_JOB_NAME
                        },
                    )
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    jacksonObjectMapper().writeValue(byteArrayOutputStream, sendFileClientData)
                    client.sendByteArray(byteArrayOutputStream.toByteArray()) // TODO ENCRYPT BEFORE RELEASE

                    val sendFileServerData: SendFileServerData = String(
                        bytes = client.receiveByteArray(),
                        charset = StandardCharsets.UTF_8,
                    ).let { json ->
                        jacksonObjectMapper().readValue<SendFileServerData>(json)
                    }

                    if (currentJob.needDescription) {
                        currentJob = currentJob.copy(description = sendFileServerData.jobName)
                    }
                    updateTransferJob(currentJob)
                    currentJob.cloneItems().fastForEach { item ->
                        // TODO: Relative path - After release
                        client.sendBoolean(item.isFile)
                        if (item.isFile) {
                            MainActivity.mainActivity.contentResolver.openInputStream(item.uri()).use {
                                it?.sendFile(client = client, item = item) { progressBytes ->
                                    runBlocking {
                                        progressUpdateMutex.withLock {
                                            currentJob = currentJob.updateItem(
                                                item = item.copy(progressBytes = progressBytes),
                                            )
                                            updateTransferJob(job = currentJob)
                                        }
                                    }
                                }
                            }
                        }
                        client.receiveContinue()
                        updateTransferJobCurrentItem(job = currentJob) // TODO needed? - After release
                    }
                } else {
                    logError(log = "Password refused")
                }
                finishTransferJob(currentJob)
                updateNotification()
            }
        }
    }
}

class SendFileClientData(
    val items: List<Item>,
    val aesKey: ByteArray,
    val jobName: String,
    val nameFlag: String,
)

class SendFileServerData(
    val jobName: String,
    val pathLimit: Int,
)

fun OutgoingJob.createClient() = HMeadowSocketClient(
    ipAddress = destination.ip,
    port = port,
    operationTimeoutMillis = 2000,
    handshakeTimeoutMillis = 2000,
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

fun InputStream.sendFile(
    client: HMeadowSocketClient,
    item: Item,
    onProgressUpdate: (Long) -> Unit,
) {
    client.sendFile(
        stream = this,
        name = item.name,
        size = item.sizeBytes,
        progressPrecision = 0.01,
        onProgressUpdate = onProgressUpdate,
    )
}
