package com.example.modul_4_pract_1_4

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {

    private var randomNumberService: RandomNumberService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RandomNumberService.RandomNumberBinder
            randomNumberService = binder.getService()
            isBound = true

            // слушатель
            randomNumberService?.registerListener(numberUpdateListener)

            randomNumberService?.startGenerating()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            randomNumberService = null
            isBound = false
        }
    }

    private val numberUpdateListener = object : RandomNumberService.NumberUpdateListener {
        override fun onNumberUpdated(number: Int) {
            _currentNumber.value = number
        }
    }

    private val _currentNumber = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_4_pract_14Theme {
                RandomNumberScreen(
                    currentNumber = _currentNumber.value,
                    isConnected = isBound,
                    onConnect = { bindToService() },
                    onDisconnect = { unbindFromService() }
                )
            }
        }
    }

    private fun bindToService() {
        if (!isBound) {
            val intent = Intent(this, RandomNumberService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            println("Activity: Запрос на подключение к сервису")
        }
    }

    private fun unbindFromService() {
        if (isBound) {

            randomNumberService?.stopGenerating()
            randomNumberService?.unregisterListener(numberUpdateListener)
            unbindService(serviceConnection)
            isBound = false
            randomNumberService = null
            _currentNumber.value = 0
            println("Activity: Отключение от сервиса")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindFromService()
        }
    }
}

@Composable
fun RandomNumberScreen(
    currentNumber: Int,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Случайные числа",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.size(200.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isConnected) "$currentNumber" else "---",
                    fontSize = if (isConnected) 48.sp else 32.sp,
                    color = if (isConnected)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onConnect,
                enabled = !isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("Подключиться")
            }

            Button(
                onClick = onDisconnect,
                enabled = isConnected,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Отключиться")
            }
        }
    }
}

