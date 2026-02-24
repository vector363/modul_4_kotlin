package com.example.modul_4_pract_1_4.old_pract

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

    // Используем StateFlow для хранения курса
    private val _rate = MutableStateFlow(0.0)
    val rate: StateFlow<Double> = _rate.asStateFlow()

    // Для отслеживания предыдущего значения (чтобы показать стрелку)
    private var previousRate by mutableStateOf(0.0)

    // Состояния загрузки
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Для определения направления изменения курса
    val isIncreasing: Boolean
        get() = _rate.value > previousRate

    val isDecreasing: Boolean
        get() = _rate.value < previousRate

    init {
        // При создании ViewModel запускаем автоматическое обновление
        startAutoUpdate()
        // И сразу загружаем первый курс
        loadNewRate()
    }

    private fun startAutoUpdate() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // Каждые 5 секунд
                loadNewRate()
            }
        }
    }

    fun loadNewRate() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Сохраняем предыдущее значение
                previousRate = _rate.value

                // Имитация загрузки с задержкой
                delay(1000)

                // Генерируем случайный курс (базовое значение ~90, разброс ±2)
                val newRate = generateRandomRate()

                // Обновляем StateFlow
                _rate.update { newRate }

            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Принудительное обновление (для кнопки)
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

    // Форматирование курса для отображения
    fun getFormattedRate(): String {
        return String.format("%.2f ₽", _rate.value)
    }
}