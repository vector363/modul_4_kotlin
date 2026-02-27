package com.example.modul_4_pract_1_4

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderManager {
    private const val REQUEST_CODE = 1001

    fun scheduleReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)  // Через 1 минуту
            set(Calendar.SECOND, 0)
        }


        // Для Android 12+ нужно проверять разрешение
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        // Сохраняем состояние
        saveReminderState(context, true)
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        // Сохраняем состояние
        saveReminderState(context, false)
    }

    private fun saveReminderState(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
    }

    fun isReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("reminder_enabled", false)
    }

    fun getNextReminderTime(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val today = Calendar.getInstance()
        return if (calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            "сегодня в 20:00"
        } else {
            "завтра в 20:00"
        }
    }
}
