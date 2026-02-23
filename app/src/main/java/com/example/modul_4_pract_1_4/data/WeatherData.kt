package com.example.modul_4_pract_1_4.data

data class WeatherData(
    val city: String,
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Int
)

data class WeatherReport(
    val cities: List<WeatherData>,
    val averageTemperature: Double,
    val completionTime: String
)