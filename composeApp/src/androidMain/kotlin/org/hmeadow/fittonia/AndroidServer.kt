package org.hmeadow.fittonia

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.IBinder
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AndroidServer : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override fun onBind(intent: Intent?): IBinder? {
        println("onBind")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        println("onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            constructNotification(current = 0, total = 10),
            FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        println("onStartCommand")
        this.launch {
            startThread()
        }
        return START_STICKY // If the service is killed, it will be automatically restarted.
    }

    private fun startThread() {
        repeat(10) {
            Thread.sleep(2000)
            updateNotification(current = it)
            println("Thread progress $it")
        }
    }

    private fun constructNotification(current: Int, total: Int) = Notification
        .Builder(this, getString(R.string.send_receive_channel_id))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOnlyAlertOnce(true)
        .setContentTitle(getString(R.string.send_receive_foreground_service_notification_title))
        .setContentText(getString(R.string.send_receive_foreground_service_notification_content, current, total))
        .build()

    private fun updateNotification(current: Int) {
        val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.notify(NOTIFICATION_ID, constructNotification(current = current, total = 10))
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
    }

    companion object {
        const val NOTIFICATION_ID = 455
    }
}
