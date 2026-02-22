package com.example.modul_4_pract_1_4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.modul_4_pract_1_4.ui.theme.Modul_4_pract_14Theme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
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
                TimerNotification(context = this@MainActivity)
            }
        }
    }
}


@Composable
fun TimerNotification(context: Context){
    var timerValue by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }


    val broadcastReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == TimerService.TIMER_UPDATE_ACTION) {
                    val seconds = intent.getIntExtra(TimerService.TIMER_VALUE_EXTRA, 0)
                    Log.d("MAIN_ACTIVITY", "📱 Получен broadcast: $seconds сек")
                    timerValue = seconds
                }
            }
        }
    }

    DisposableEffect(Unit) {
        Log.d("MAIN_ACTIVITY", "📱 Регистрация receiver")
        val filter = IntentFilter(TimerService.TIMER_UPDATE_ACTION)
        context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        onDispose {
            Log.d("MAIN_ACTIVITY", "📱 Отмена регистрации receiver")
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    fun startTimer() {
        val intent = Intent(context, TimerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        isTimerRunning = true
    }

    fun stopTimer(){
        val intent = Intent(context, TimerService::class.java)
        isTimerRunning = false
        context.stopService(intent)
        timerValue = 0
    }



    Column(
        modifier = Modifier.fillMaxSize()
            .padding(top = 25.dp)
            .fillMaxSize()
            .padding(top = 50.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Таймер и точка",
            fontSize = 50.sp,
            modifier = Modifier.padding(bottom = 32.dp, top = 100.dp),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "$timerValue сек",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(onClick = { startTimer() },
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
        ) {
            Text("Старт", fontSize=18.sp)
        }


        Button(onClick = { stopTimer() },
            modifier = Modifier
                .padding(top = 30.dp)
                .width(120.dp)
                .height(60.dp)
        ) {
            Text("Стоп", fontSize=18.sp)
        }

    }
}



