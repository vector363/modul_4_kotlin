package com.example.modul_4_pract_1_4

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class USDtoRubViewModel : ViewModel() {

    // StateFlow для хранения курса
    private val _rate = MutableStateFlow(0.0)

    private var previousRate by mutableStateOf(0.0)

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set


    init {
        startAutoUpdate()
        loadNewRate()
    }

    private fun startAutoUpdate() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                loadNewRate()
            }
        }
    }

    fun loadNewRate() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                previousRate = _rate.value
                delay(1000)
                val newRate = generateRandomRate()

                _rate.update { newRate }

            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun forceUpdate() {
        loadNewRate()
    }

    private fun generateRandomRate(): Double {
        val baseRate = 90.0
        val variation = Random.nextDouble(-2.0, 2.0)
        return (baseRate + variation).roundToTwoDecimals()
    }

    private fun Double.roundToTwoDecimals(): Double {
        return Math.round(this * 100) / 100.0
    }

    fun getFormattedRate(): String {
        return String.format("%.2f ₽", _rate.value)
    }
}