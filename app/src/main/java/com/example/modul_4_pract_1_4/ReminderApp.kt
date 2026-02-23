package com.example.modul_4_pract_1_4


import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pill_channel",
                "Pill Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for daily pill reminders"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}