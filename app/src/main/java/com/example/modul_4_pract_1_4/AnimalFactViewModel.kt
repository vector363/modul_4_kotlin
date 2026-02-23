package com.example.modul_4_pract_1_4

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.random.Random


class AnimalFactViewModel : ViewModel() {

    var currentFact by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun getRandomFact(): Flow<String> = flow {
        val delayTime = Random.nextLong(1500, 3000)
        delay(delayTime)
        emit(AnimalFacts.getRandomFact())
    }

    fun loadNewFact() {
        isLoading = true

        viewModelScope.launch {
            getRandomFact().collect { fact ->
                currentFact = fact
                isLoading = false
            }
        }
    }
}