package com.example.modul_4_pract_1_4

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File


class PhotoCompressionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val photoPath = inputData.getString("photo_path") ?: return@withContext Result.failure(
                Data.Builder().putString("error", "Не указан путь к фото").build()
            )

            for (i in 1..10) {
                delay(300)
                val progress = i * 10
                setProgressAsync(
                    Data.Builder()
                        .putInt("progress", progress)
                        .putString("stage", "Сжатие")
                        .build()
                )
            }

            val compressedPhotoPath = compressPhoto(photoPath)

            val outputData = Data.Builder()
                .putString("compressed_photo_path", compressedPhotoPath)
                .putString("original_photo_path", photoPath)
                .build()

            Result.success(outputData)
        }
    }

    private fun compressPhoto(inputPath: String): String {
        val inputFile = File(inputPath)
        val outputFile = File(applicationContext.cacheDir, "compressed_${inputFile.name}")

        return outputFile.absolutePath
    }
}