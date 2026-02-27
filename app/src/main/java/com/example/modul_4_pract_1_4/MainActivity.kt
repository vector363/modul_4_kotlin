package com.example.modul_4_pract_1_4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        setContent {
            Modul_4_pract_14Theme {
                OneTimeTimerScreen()
            }
        }
    }
}

@Composable
fun OneTimeTimerScreen() {
    val context = LocalContext.current
    var secondsInput by remember { mutableStateOf("") }
    var isTimerRunning by remember { mutableStateOf(false) }

    // receiver для получения события о завершении таймера
    val timerFinishedReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == OneTimeTimerService.TIMER_FINISHED_ACTION) {
                    isTimerRunning = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter(OneTimeTimerService.TIMER_FINISHED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(timerFinishedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(timerFinishedReceiver, filter)
        }

        onDispose {
            context.unregisterReceiver(timerFinishedReceiver)
        }
    }

    fun startTimer() {
        val seconds = secondsInput.toIntOrNull()

        val intent = Intent(context, OneTimeTimerService::class.java).apply {
            putExtra(OneTimeTimerService.EXTRA_SECONDS, seconds)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        isTimerRunning = true
        secondsInput = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Одноразовый таймер",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        OutlinedTextField(
            value = secondsInput,
            onValueChange = { secondsInput = it.filter { char -> char.isDigit() } },
            label = { Text("Введите количество секунд") },
            placeholder = { Text("например: 30") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = !isTimerRunning,
            singleLine = true
        )

        Button(
            onClick = { startTimer() },
            enabled = !isTimerRunning && secondsInput.isNotBlank(),
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        ) {
            Text("Запустить таймер", fontSize = 18.sp)
        }

        if (isTimerRunning) {
            Text(
                text = "Таймер запущен...",
                modifier = Modifier.padding(top = 24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


