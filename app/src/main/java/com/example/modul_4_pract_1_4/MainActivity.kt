package com.example.modul_4_pract_1_4

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.modul_4_pract_1_4.ui.theme.Modul_4_pract_14Theme
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random



class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_4_pract_14Theme {
            }
        }

        loadAllData()
    }

    fun loadAllData() {
        val time = measureTimeMillis {
            runBlocking {

                val usersDeferred = async {
                    try {
                        LoadUsers()
                    } catch (e: Exception) {
                        emptyList<String>()
                    }
                }
                val salesDeferred = async {
                    try {
                        LoadSales()
                    } catch (e: Exception) {
                        emptyMap<String, Int>()
                    }
                }

                val weatherDeferred = async {
                    try {
                        LoadWeather()
                    } catch (e: Exception) {
                        emptyList<String>()
                    }
                }

                val users = usersDeferred.await()
                val sales = salesDeferred.await()
                val weather = weatherDeferred.await()

                Log.d(TAG, "Пользователи: $users")
                Log.d(TAG, "Продажи: $sales")
                Log.d(TAG, "Погода: $weather")

            }
        }
        Log.d(TAG, "Общее время выполнения: ${time / 1000.0} секунд")
    }

    suspend fun LoadUsers():List<String>{
        delay(1800)

        if (Random.nextFloat() < 0.1f) {
            throw Exception("Ошибка соединения при загрузке пользователей")
        }

        val jsonString = loadJsonFromAssets("users.json")
        val listType = object : TypeToken<List<User>>() {}.type
        val users: List<User> = Gson().fromJson(jsonString, listType)
        val names = users.map { it.name }
        return names
    }


    suspend fun LoadSales():Map<String, Int>{
        delay(1200)

        if (Random.nextFloat() < 0.2f) {
            throw Exception("Ошибка сервера при загрузке продаж")
        }

        val jsonString = loadJsonFromAssets("sales.json")
        val salesData: SalesData = Gson().fromJson(jsonString, SalesData::class.java)
        val salesMap = salesData.items.associate { it.product to it.qty }
        return salesMap

    }

    suspend fun LoadWeather():List<String>{
        delay(2500)

        if (Random.nextFloat() < 0.2f) {
            throw Exception("Таймаут при загрузке погоды")
        }

        val jsonString = loadJsonFromAssets("weather.json")
        val listType = object : TypeToken<List<Weather>>() {}.type
        val weatherList: List<Weather> = Gson().fromJson(jsonString, listType)
        val weatherStrings = weatherList.map { "${it.city}: ${it.temp}°C" }
        return weatherStrings
    }


    private fun loadJsonFromAssets(filename: String): String {
        return assets.open(filename).bufferedReader().use { it.readText() }
    }


    data class User(
        val id: Int,
        val name: String
    )

    data class SaleItem(
        val product: String,
        val qty: Int,
        val revenue: Int
    )

    data class SalesData(
        val today: String,
        val items: List<SaleItem>
    )
    data class Weather(
        val city: String,
        val temp: Int,
        val condition: String
    )

}




