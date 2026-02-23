package com.example.modul_4_pract_1_4.old_pract

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*

class RandomNumberService : Service() {

    private val binder = RandomNumberBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isGenerating = false
    private var currentNumber = 0
    private var listeners = mutableListOf<NumberUpdateListener>()

    interface NumberUpdateListener {
        fun onNumberUpdated(number: Int)
    }

    inner class RandomNumberBinder : Binder() {
        fun getService(): RandomNumberService = this@RandomNumberService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGenerating()
        serviceScope.cancel()
    }

    fun startGenerating() {
        if (isGenerating) return

        isGenerating = true

        serviceScope.launch {
            while (isGenerating) {
                currentNumber = (0..100).random()

                withContext(Dispatchers.Main) {
                    listeners.forEach { it.onNumberUpdated(currentNumber) }
                }

                delay(1000)
            }
        }
    }

    fun stopGenerating() {
        isGenerating = false
    }

    fun registerListener(listener: NumberUpdateListener) {
        listeners.add(listener)
        // Сразу отправляем текущее число новому слушателю
        listener.onNumberUpdated(currentNumber)
    }

    fun unregisterListener(listener: NumberUpdateListener) {
        listeners.remove(listener)
    }
}