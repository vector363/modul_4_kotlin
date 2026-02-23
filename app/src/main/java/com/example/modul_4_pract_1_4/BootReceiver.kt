package com.example.modul_4_pract_1_4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Проверяем, было ли включено напоминание
            if (ReminderManager.isReminderEnabled(context)) {
                ReminderManager.scheduleReminder(context)
            }
        }
    }
}