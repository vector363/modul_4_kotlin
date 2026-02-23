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

class OneTimeTimerService : Service() {
    private val CHANNEL_ID = "one_time_timer_channel"
    private val NOTIFICATION_ID = 2001
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null

    companion object {
        const val EXTRA_SECONDS = "extra_seconds"
        const val TIMER_FINISHED_ACTION = "com.example.modul_4_pract_1_4.TIMER_FINISHED"
        const val TAG = "OneTimeTimer"
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val seconds = intent?.getIntExtra(EXTRA_SECONDS, 0) ?: 0

        startForeground(NOTIFICATION_ID, createNotification("Таймер запущен", "Осталось: $seconds сек"))

        timerJob = serviceScope.launch {
            delay(seconds * 1000L)

            // Показываем уведомление о завершении
            showCompletionNotification()

            // Отправляем broadcast, чтобы сбросить состояние в UI
            val finishedIntent = Intent(TIMER_FINISHED_ACTION).apply {
                setPackage(packageName)
            }
            sendBroadcast(finishedIntent)

            delay(500)

            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun showCompletionNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)

        // Показываем финальное уведомление
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Таймер завершён!")
            .setContentText("Время вышло")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Одноразовый таймер",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о завершении таймера"
                enableVibration(true)
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
}