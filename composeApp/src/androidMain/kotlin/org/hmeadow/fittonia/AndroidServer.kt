package org.hmeadow.fittonia

import Log
import Server
import ServerLogs
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ServiceCompat
import hmeadowSocket.HMeadowSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.screens.overviewScreen.TransferJob
import org.hmeadow.fittonia.screens.overviewScreen.TransferStatus
import java.net.ServerSocket
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.random.Random

class AndroidServer : Service(), CoroutineScope, ServerLogs, Server {
    override val mLogs = mutableListOf<Log>()
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val binder = AndroidServerBinder()
    private lateinit var serverSocket: ServerSocket

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            serverSocket = ServerSocket(it.getIntExtra("org.hmeadow.fittonia.port", 0))
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            constructNotification(transferJobsActive = transferJobs.value.size),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        return START_STICKY // If the service is killed, it will be automatically restarted.
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
    }

    /* MUST BE IN 'SYNCHRONIZED' */
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
        return true // TODO
    }

    companion object {
        const val NOTIFICATION_ID = 455

        var server: MutableStateFlow<AndroidServer?> = MutableStateFlow(null)

        fun startThread(newJob: TransferJob) {
            server.value?.run {
                addTransferJob(newJob)
                updateNotification()
                launch {
                    repeat(newJob.totalItems) {
                        transferJobs.value.find { it.id == newJob.id }?.let { currentJob ->
                            Thread.sleep(abs(Random.nextLong() % 2000))
                            updateTransferJobCurrentItem(job = currentJob)
                        }
                    }
                    finishTransferJob(newJob)
                    updateNotification()
                }
            }
        }
    }
}
