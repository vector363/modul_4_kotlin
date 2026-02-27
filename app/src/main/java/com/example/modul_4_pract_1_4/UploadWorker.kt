package com.example.modul_4_pract_1_4

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val watermarkedPath = inputData.getString("watermarked_photo_path")
                ?: return@withContext Result.failure(
                    Data.Builder().putString("error", "Нет пути к фото с водяным знаком").build()
                )

            for (i in 1..15) {
                delay(200)
                val progress = (i * 100) / 15
                setProgressAsync(
                    Data.Builder()
                        .putInt("progress", progress)
                        .putString("stage", "Загрузка")
                        .build()
                )
            }

            val cloudUrl = uploadToCloud(watermarkedPath)

            val fileName = File(watermarkedPath).name
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            val outputData = Data.Builder()
                .putString("result_message", "Готово! Фото загружено")
                .putString("file_name", fileName)
                .putString("cloud_url", cloudUrl)
                .putString("completion_time", timestamp)
                .build()

            Result.success(outputData)
        }
    }

    private fun uploadToCloud(filePath: String): String {
        val fileName = File(filePath).name
        return "https://cloud.example.com/uploads/$fileName"
    }
}