package org.hmeadow.fittonia

import Log
import Server
import ServerCommandFlag
import ServerLogs
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ServiceCompat
import communicateCommand
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.hmeadow.fittonia.screens.overviewScreen.TransferJob
import org.hmeadow.fittonia.screens.overviewScreen.TransferStatus
import java.net.BindException
import java.net.ServerSocket
import java.net.SocketException
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.Path

class AndroidServer : Service(), CoroutineScope, ServerLogs, Server {
    override val mLogs = mutableListOf<Log>()
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    override var jobId: Int = 100
    private val binder = AndroidServerBinder()
    var serverSocket: ServerSocket? = null
    var serverJob: Job? = null
    private lateinit var password: String

    inner class AndroidServerBinder : Binder() {
        fun getService(): AndroidServer = this@AndroidServer
    }

    var transferJobs = MutableStateFlow<List<TransferJob>>(emptyList())
        private set

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        server.value = this
    }

    private fun initFromIntent(intent: Intent?): Boolean {
        intent?.let {
            it.getIntExtra("org.hmeadow.fittonia.port", 0).let { port ->
                if(!startServerSocket(port = port)){
                    return false
                }
            }
            password = it.getStringExtra("org.hmeadow.fittonia.password")
                ?: throw Exception("No password provided").also { serverSocket = null } // TODO
            return true
        }
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (initFromIntent(intent = intent).also { println("AndroidServer.onStartCommand() success = $it") }) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                constructNotification(transferJobsActive = transferJobs.value.size),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
            launchServerJob()
            return START_STICKY // If the service is killed, it will be automatically restarted.
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        MainActivity.mainActivityForServer?.unbindFromServer()
        stopSelf()
        return START_NOT_STICKY
    }

    private fun startServerSocket(port:Int):Boolean{
        if (port == 0) throw Exception("No port provided")
        try {
            serverSocket = ServerSocket(port)
        } catch (e: BindException) {
            serverSocket = null
            if (e.message?.contains("Address already in use") == true) {
                MainActivity.mainActivityForServer?.alert(UserAlert.PortInUse(port = port))
                return false
            } else {
                throw e
            }
        }
        return true
    }

    private fun launchServerJob(){
        serverSocket?.let { server ->
            serverJob = launch {
                while (true) {
                    yield()
                    try {
                        HMeadowSocketServer.createServerFromSocket(server).let { server ->
                            launch {
                                log("Connected to client.")
                                handleCommand(server = server, jobId = jobId)
                            }
                        }
                    }catch(e: SocketException) {
                        log(e.message ?: "Unknown server SocketException")
                        // Don't worry!
                    }
                }
            }
        }
    }

    fun restartServerSocket(port:Int){
        serverJob?.cancel()
        serverSocket?.close()
        startServerSocket(port = port)
        launchServerJob()
    }

    private fun constructNotification(transferJobsActive: Int) = Notification
        .Builder(this, getString(R.string.send_receive_channel_id))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOnlyAlertOnce(true)
        .setContentText(getString(R.string.send_receive_foreground_service_notification_content, transferJobsActive))
        .build()

    private fun updateNotification() {
        val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        synchronized(notificationManager) {
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

    /* MUST BE IN 'SYNCHRONIZED' */ // TODO USE MUTEX
    private fun updateTransferJob(job: TransferJob) {
        transferJobs.value = (transferJobs.value.filterNot { it.id == job.id } + job).sortedBy { it.id }
    }

    private fun findJob(job: TransferJob): TransferJob? = transferJobs.value.find { it.id == job.id }

    private fun addTransferJob(job: TransferJob) {
        synchronized(transferJobs) {
            updateTransferJob(job = job)
        }
    }

    private fun updateTransferJobCurrentItem(job: TransferJob) {
        synchronized(transferJobs) {
            findJob(job)?.let { job ->
                updateTransferJob(job.copy(currentItem = job.nextItem))
            }
        }
    }

    private fun finishTransferJob(job: TransferJob) {
        synchronized(transferJobs) {
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

    override fun onAddDestination(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
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

    override fun onSendFiles(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to send files to this server, password refused.", jobId = jobId)
        } else {
            log("Client attempting to send files.", jobId = jobId)

            when (server.receiveString()) {
                ServerFlagsString.NEED_JOB_NAME -> ">:3".also {
                    log("Server generated job name: $it", jobId = jobId)
                    server.sendString(it)
                }

                ServerFlagsString.HAVE_JOB_NAME -> server.receiveString().also {
                    log("Client provided job name: $it", jobId = jobId)
                }

                else -> throw Exception() // TODO
            }

            server.sendInt(128) // Not sure android has the same path limits as desktop.

            server.receiveContinue()
        }
    }

    override fun onSendMessage(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to send a message, password refused.", jobId = jobId)
        } else {
            log("Client message: ${server.receiveString()}", jobId = jobId)
            server.sendConfirmation()
        }
    }

    override fun onInvalidCommand(unknownCommand: String) {
        logWarning("Received invalid server command from client: $unknownCommand", jobId = jobId)
    }

    companion object {
        const val NOTIFICATION_ID = 455

        var server: MutableStateFlow<AndroidServer?> = MutableStateFlow(null)

        suspend fun startSending(job: TransferJob) {
            if(server.value == null){
                MainActivity.mainActivity.attemptStartServer()
                server.first()
            }

            server.value?.run {
                job.copy(
                    description = if(job.needDescription) "Connecting..." else job.description,
                ).let { realJob ->
                    addTransferJob(realJob)
                    updateNotification()
                    launch {
                        try {
                            val client = HMeadowSocketClient(
                                ipAddress = realJob.destination.ip,
                                port = realJob.port,
                                operationTimeoutMillis = 2000,
                                handshakeTimeoutMillis = 2000L,
                            )
                            if (client.communicateCommand(
                                    commandFlag = ServerCommandFlag.SEND_FILES,
                                    password = realJob.destination.password,
                                    onSuccess = { },
                                    onPasswordRefused = { logError("Server refused password.") },
                                    onFailure = { logError("Connected, but request refused.") },
                                )
                            ) {
                                log("Password accepted")
                                realJob.description.takeIf{ !realJob.needDescription }?.let { jobName ->
                                    client.sendString(ServerFlagsString.HAVE_JOB_NAME)
                                    client.sendString(jobName)
                                } ?: run {
                                    client.sendString(ServerFlagsString.NEED_JOB_NAME)
                                    updateTransferJob(job.copy(description = client.receiveString()))
                                }

                                log("path length: " + client.receiveInt())
                                client.sendContinue()
                            } else {
                                logError("Password refused")
                            }
                            updateTransferJobCurrentItem(job = realJob)
                        } catch (e: HMeadowSocket.HMeadowSocketError) { // TODO Better error handling & messaging.
                            e.hmMessage?.let { logError(it) }
                            e.message?.let { logError(it) }
                        }
                        finishTransferJob(realJob)
                        updateNotification()
                    }
                }
            }
        }
    }
}
