package com.example.modul_4_pract_1_4.old_pract

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.modul_4_pract_1_4.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random


class CityWeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val foregroundInfo = NotificationHelper.createForegroundInfo(
            applicationContext,
            0,
            "Начинаем загрузку..."
        )
        setForegroundAsync(foregroundInfo)

        return withContext(Dispatchers.IO) {
            try {
                val city = inputData.getString("city") ?: "Unknown"
                val cityIndex = inputData.getInt("city_index", 0)

                val totalSteps = 10
                for (i in 1..totalSteps) {
                    delay(300)
                    val progress = (i * 100) / totalSteps

                    setProgressAsync(
                        workDataOf(
                            "progress" to progress,
                            "city" to city,
                            "stage" to "Загрузка $city"
                        )
                    )

                }

                val weather = WeatherData(
                    city = city,
                    temperature = Random.nextInt(-25, 25),
                    condition = listOf("Солнечно", "Облачно", "Дождь", "Снег", "Ветрено").random(),
                    humidity = Random.nextInt(40, 90),
                    windSpeed = Random.nextInt(0, 15)
                )

                val outputData = workDataOf(
                    "city_$cityIndex" to weather.city,
                    "temperature_$cityIndex" to weather.temperature,
                    "condition_$cityIndex" to weather.condition,
                    "humidity_$cityIndex" to weather.humidity,
                    "windSpeed_$cityIndex" to weather.windSpeed,
                    "city_index" to cityIndex
                )

                Result.success(outputData)

            } catch (e: Exception) {
                Result.failure(workDataOf("error" to e.message))
            }
        }
    }
}