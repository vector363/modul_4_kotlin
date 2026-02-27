package com.example.modul_4_pract_1_4


import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.modul_4_pract_1_4.ui.theme.Modul_4_pract_14Theme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        setContent {
            Modul_4_pract_14Theme {
                ReminderScreen()
            }
        }
    }
}

@Composable
fun ReminderScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isProcessing by remember { mutableStateOf(false) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var nextReminderTime by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // проверка разрешений при запуске
    LaunchedEffect(Unit) {
        reminderEnabled = ReminderManager.isReminderEnabled(context)
        if (reminderEnabled) {
            nextReminderTime = ReminderManager.getNextReminderTime()
        }
    }

    fun enableReminder() {
        isProcessing = true
        errorMessage = null

        try {
            ReminderManager.scheduleReminder(context)
            reminderEnabled = true
            nextReminderTime = ReminderManager.getNextReminderTime()
        } catch (e: Exception) {
            errorMessage = "Ошибка: ${e.message}"
        } finally {
            isProcessing = false
        }
    }

    fun disableReminder() {
        isProcessing = true
        errorMessage = null
        try {
            ReminderManager.cancelReminder(context)
            reminderEnabled = false
            nextReminderTime = ""
        } catch (e: Exception) {
            errorMessage = "Ошибка: ${e.message}"
        } finally {
            isProcessing = false
        }
    }

    fun toggleReminder() {
        if (reminderEnabled) {
            disableReminder()
        } else {
            enableReminder()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Напоминание о таблетке",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (reminderEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (reminderEnabled) "🟢 Включено" else "⚪ Выключено",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp,
                    color = if (reminderEnabled)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "$errorMessage",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { toggleReminder() },
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (reminderEnabled) "Выключить напоминание" else "Включить напоминание",
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Напоминание будет приходить ежедневно в 20:00",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



