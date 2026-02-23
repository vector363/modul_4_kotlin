package com.example.modul_4_pract_1_4

import android.content.Context
import androidx.work.*
import com.example.modul_4_pract_1_4.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeatherReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val allData = inputData.keyValueMap
            val cities = mutableListOf<WeatherData>()

            for (i in 0..3) {
                val cityKey = "city_$i"
                val tempKey = "temperature_$i"
                val conditionKey = "condition_$i"
                val humidityKey = "humidity_$i"
                val windKey = "windSpeed_$i"

                val city = allData["city"] as? String
                    ?: allData[cityKey] as? String
                    ?: continue

                val temperature = allData["temperature"] as? Int
                    ?: allData[tempKey] as? Int
                    ?: 0

                val condition = allData["condition"] as? String
                    ?: allData[conditionKey] as? String
                    ?: "Неизвестно"

                val humidity = allData["humidity"] as? Int
                    ?: allData[humidityKey] as? Int
                    ?: 50

                val windSpeed = allData["windSpeed"] as? Int
                    ?: allData[windKey] as? Int
                    ?: 5

                if (city != "Unknown" && temperature != 0) {
                    cities.add(WeatherData(city, temperature, condition, humidity, windSpeed))
                }
            }

            if (cities.isEmpty()) {
                val city = allData["city"] as? String
                val temperature = allData["temperature"] as? Int

                if (city != null && temperature != null) {
                    cities.add(
                        WeatherData(
                            city = city,
                            temperature = temperature,
                            condition = allData["condition"] as? String ?: "Неизвестно",
                            humidity = allData["humidity"] as? Int ?: 50,
                            windSpeed = allData["windSpeed"] as? Int ?: 5
                        )
                    )
                }
            }

            if (cities.isEmpty()) {
                return@withContext Result.failure(
                    workDataOf("error" to "Нет данных о городах")
                )
            }

            for (i in 1..5) {
                delay(200)
                setProgressAsync(
                    workDataOf(
                        "progress" to (i * 20),
                        "stage" to "Формирование отчета"
                    )
                )
            }

            val avgTemp = cities.map { it.temperature }.average()

            val report = buildString {
                appendLine("ОТЧЕТ О ПОГОДЕ")
                appendLine()
                cities.forEachIndexed { index, weather ->
                    appendLine("${index + 1}. ${weather.city}: ${weather.temperature}°C, ${weather.condition}")
                }
                appendLine()
                appendLine("Средняя температура: ${"%.1f".format(avgTemp)}°C")
            }


            val outputData = workDataOf(
                "report" to report,
                "average_temperature" to avgTemp,
                "cities_count" to cities.size,
            )

            Result.success(outputData)
        }
    }
}