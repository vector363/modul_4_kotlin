package com.example.modul_4_pract_1_4.old_pract


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*


class TimerService : Service()  {
    private val CHANNEL_ID = "timer_channel"
    private val NOTIFICATION_ID = 1001
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var timerJob: Job? = null
    private var secondsElapsed = 0

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification(0))
        startTimer()
        return START_STICKY
    }

    private fun startTimer() {
        timerJob = serviceScope.launch {
            while (true) {
                delay(1000)
                secondsElapsed++

                updateNotification(secondsElapsed)

                // Отправляем broadcast с указанием пакета
                val intent = Intent(TIMER_UPDATE_ACTION).apply {
                    putExtra(TIMER_VALUE_EXTRA, secondsElapsed)
                    setPackage(packageName)
                }
                sendBroadcast(intent)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Таймер",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отображение времени таймера"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(seconds: Int): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Счётчик времени")
            .setContentText("Прошло $seconds секунд")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    private fun updateNotification(seconds: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(seconds))
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }

    companion object {
        const val TIMER_UPDATE_ACTION = "com.example.modul_4_pract_1_4.TIMER_UPDATE"
        const val TIMER_VALUE_EXTRA = "timer_value"
    }

}
