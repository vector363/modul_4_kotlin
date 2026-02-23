package com.example.modul_4_pract_1_4

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

class WatermarkWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val compressedPath = inputData.getString("compressed_photo_path")
                ?: return@withContext Result.failure(
                    Data.Builder().putString("error", "Нет пути к сжатому фото").build()
                )

            val originalPath = inputData.getString("original_photo_path") ?: "unknown"

            for (i in 1..8) {
                delay(350)
                val progress = i * 12
                setProgressAsync(
                    Data.Builder()
                        .putInt("progress", progress)
                        .putString("stage", "Водяной знак")
                        .build()
                )
            }

            val watermarkedPath = addWatermark(compressedPath)

            val outputData = Data.Builder()
                .putString("watermarked_photo_path", watermarkedPath)
                .putString("original_photo_path", originalPath)
                .build()

            Result.success(outputData)
        }
    }

    private fun addWatermark(inputPath: String): String {
        val inputFile = File(inputPath)
        val outputFile = File(applicationContext.cacheDir, "watermarked_${inputFile.name}")

        return outputFile.absolutePath
    }
}