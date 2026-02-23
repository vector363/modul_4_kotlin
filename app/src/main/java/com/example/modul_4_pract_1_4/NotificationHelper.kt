package com.example.modul_4_pract_1_4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager

object NotificationHelper {
    const val CHANNEL_ID = "weather_channel"
    const val NOTIFICATION_ID = 1001
    const val CHANNEL_NAME = "Weather Service"
    const val CHANNEL_DESCRIPTION = "Shows weather download progress"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createForegroundInfo(context: Context, progress: Int, stage: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Загрузка погоды")
            .setContentText(stage)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ForegroundInfo(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            return ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    fun createCompletedNotification(context: Context, report: String): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Отчет о погоде готов")
            .setContentText(report.substring(0, minOf(100, report.length)) + "...")
            .setStyle(NotificationCompat.BigTextStyle().bigText(report))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
}