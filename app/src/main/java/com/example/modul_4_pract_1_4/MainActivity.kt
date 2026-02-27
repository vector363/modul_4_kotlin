package com.example.modul_4_pract_1_4


import android.content.Context
import android.graphics.Insets.add
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.io.File
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.workDataOf
import com.example.modul_4_pract_1_4.data.WeatherData
import kotlinx.coroutines.launch

enum class CityLoadState {
    IDLE,
    LOADING,
    COMPLETED
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        setContent {
            Modul_4_pract_14Theme {
                WeatherScreen()
            }
        }
    }
}

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val coroutineScope = rememberCoroutineScope()

    val cities = listOf("Москва", "Лондон", "Нью-Йорк", "Токио")

    val cityStates = remember {
        mutableStateListOf<CityLoadState>().apply {
            cities.forEach { add(CityLoadState.IDLE) }
        }
    }

    val cityWeatherData = remember {
        mutableStateListOf<WeatherData?>().apply {
            cities.forEach { add(null) }
        }
    }

    var isDownloading by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var currentStage by remember { mutableStateOf("Ожидание") }
    var currentProgress by remember { mutableStateOf(0) }

    // Отслеживаем прогресс
    val workInfos = workManager
        .getWorkInfosForUniqueWorkFlow("weather_download")
        .collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(workInfos.value) {
        workInfos.value.forEach { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.RUNNING -> {
                    isDownloading = true
                    val progress = workInfo.progress.getInt("progress", 0)

                    currentProgress = progress
                    currentStage = "Загрузка..."

                    // Обновляем состояние конкретного города
                    val city = workInfo.progress.getString("city")
                    val cityIndex = cities.indexOf(city)
                    if (cityIndex >= 0) {
                        cityStates[cityIndex] = CityLoadState.LOADING
                    }
                }
                WorkInfo.State.SUCCEEDED -> {
                    if (workInfo.tags.contains("city")) {
                        // Получаем индекс города из выходных данных
                        val cityIndex = workInfo.outputData.getInt("city_index", -1)

                        if (cityIndex >= 0 && cityIndex < cities.size) {
                            // Получаем данные по индексу
                            val city = workInfo.outputData.getString("city_$cityIndex") ?: return@forEach
                            val temperature = workInfo.outputData.getInt("temperature_$cityIndex", 0)
                            val condition = workInfo.outputData.getString("condition_$cityIndex") ?: ""
                            val humidity = workInfo.outputData.getInt("humidity_$cityIndex", 0)
                            val windSpeed = workInfo.outputData.getInt("windSpeed_$cityIndex", 0)

                            // Обновляем состояние
                            cityStates[cityIndex] = CityLoadState.COMPLETED

                            // Сохраняем данные о погоде
                            cityWeatherData[cityIndex] = WeatherData(
                                city = city,
                                temperature = temperature,
                                condition = condition,
                                humidity = humidity,
                                windSpeed = windSpeed
                            )
                        }
                    }
                    if (workInfo.tags.contains("report")) {
                        isDownloading = false
                        report = workInfo.outputData.getString("report") ?: "Отчет не получен"
                        currentStage = "Готово!"
                        currentProgress = 100

                        // Показываем уведомление
                        val notification = NotificationHelper.createCompletedNotification(
                            context,
                            report
                        )
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                                as android.app.NotificationManager
                        notificationManager.notify(NotificationHelper.NOTIFICATION_ID + 1, notification)
                    }
                }
                WorkInfo.State.FAILED -> {
                    isDownloading = false
                    error = workInfo.outputData.getString("error") ?: "Неизвестная ошибка"
                    currentStage = "Ошибка!"


                }
                WorkInfo.State.CANCELLED -> {
                    isDownloading = false
                    currentStage = "Отменено"
                }
                else -> {}
            }
        }
    }

    fun startWeatherDownload() {
        // сброс состояния
        cities.indices.forEach { index ->
            cityStates[index] = CityLoadState.IDLE
            cityWeatherData[index] = null
        }

        coroutineScope.launch {
            try {
                // создаем Worker'ы для каждого города
                val cityWorkers = cities.mapIndexed { index, city ->
                    OneTimeWorkRequestBuilder<CityWeatherWorker>()
                        .setInputData(
                            workDataOf(
                                "city" to city,
                                "city_index" to index
                            )
                        )
                        .addTag("city")
                        .build()
                }

                // создаем финальный Worker для отчета
                val reportWorker = OneTimeWorkRequestBuilder<WeatherReportWorker>()
                    .addTag("report")
                    .build()

                // запускаем все параллельно, затем финальный
                workManager
                    .beginUniqueWork(
                        "weather_download",
                        ExistingWorkPolicy.REPLACE,
                        cityWorkers
                    )
                    .then(reportWorker)
                    .enqueue()

            } catch (e: Exception) {
                error = "Ошибка запуска: ${e.message}"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp, top=50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Прогноз погоды",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$currentStage",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cities.forEachIndexed { index, city ->
                CityCard(
                    city = city,
                    state = cityStates[index],
                    weatherData = cityWeatherData[index]
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { startWeatherDownload() },
            enabled = !isDownloading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isDownloading) "Загрузка..." else "Собрать прогноз")
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (report.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.LightGray
                )
            ) {
                Text(
                    text = report,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CityCard(
    city: String,
    state: CityLoadState,
    weatherData: WeatherData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = city,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp
                )


                when (state) {
                    CityLoadState.IDLE -> {
                        Text(
                            text = "Ожидание",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp
                        )
                    }
                    CityLoadState.LOADING -> {
                        Text(
                            text = "Загрузка...",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp
                        )
                    }
                    CityLoadState.COMPLETED -> {
                        Text(
                            text = "Готово",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp
                        )
                    }
                }

            }
            //центр
            if (weatherData != null) {
                Text(
                    text = "${weatherData.condition}",
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            when (state) {
                CityLoadState.IDLE -> {

                }
                CityLoadState.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                CityLoadState.COMPLETED -> {
                    if (weatherData != null) {
                        Text(
                            text = "${weatherData.temperature}°C",
                            fontSize = 25.sp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


