package com.example.modul_4_pract_1_4

import android.app.Application

class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}