package com.example.modul_4_pract_1_4

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Insets.add
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_4_pract_14Theme {
                LocationScreen()
            }
        }
    }
}

@Composable
fun LocationScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("") }
    var statusFinder by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun getAddress() {
        coroutineScope.launch {
            isProcessing = true
            errorMessage = null

            try {
                // Получаем клиент для работы с геолокацией
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                // Запрашиваем текущее местоположение
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    com.google.android.gms.tasks.CancellationTokenSource().token
                ).await()

                if (location != null) {
                    // Показываем координаты в процессе
                    currentAddress = "Координаты получены, определяем адрес..."
                    statusFinder = true

                    // Получаем адрес через Geocoder
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        // Формируем полный адрес
                        val fullAddress = buildString {
                            append(address.getAddressLine(0))
                        }
                        currentAddress = fullAddress
                    } else {
                        // Если адрес не найден, показываем координаты
                        currentAddress = String.format(
                            Locale.getDefault(),
                            "Адрес не найден\nКоординаты: %.6f, %.6f",
                            location.latitude,
                            location.longitude
                        )
                    }
                } else {
                    errorMessage = "Не удалось получить координаты"
                }

            } catch (e: SecurityException) {
                errorMessage = "Ошибка доступа к геолокации"
            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.message}"
            } finally {
                isProcessing = false
            }
        }
    }

    // Лаунчер для запроса разрешения
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            getAddress()
        } else {
            errorMessage = "Разрешение на геолокацию не предоставлено"
        }
    }

    fun requestPermissionAndGetAddress() {
        if (hasLocationPermission) {
            getAddress()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp, top=50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {

        Text(
            text = when {
                errorMessage != null -> "$errorMessage"
                statusFinder -> currentAddress
                else -> "Определить адрес"
            },
            style = MaterialTheme.typography.titleLarge,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                requestPermissionAndGetAddress()
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isProcessing) "Обработка..." else "Получить мой адрес")
        }
    }
}




