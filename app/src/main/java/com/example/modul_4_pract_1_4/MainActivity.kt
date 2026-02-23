package com.example.modul_4_pract_1_4


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.modul_4_pract_1_4.ui.theme.Modul_4_pract_14Theme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.io.File
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_4_pract_14Theme {
                PhotoProcessingScreen()
            }
        }
    }
}

@Composable
fun PhotoProcessingScreen() {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)

    var currentStatus by remember { mutableStateOf("Готов к обработке") }
    var currentProgress by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }

    val workInfos = workManager
        .getWorkInfosForUniqueWorkFlow("photo_processing")
        .collectAsStateWithLifecycle(initialValue = emptyList())


    LaunchedEffect(workInfos.value) {
        workInfos.value.forEach { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.RUNNING -> {
                    isProcessing = true
                    val stage = workInfo.progress.getString("stage") ?: ""
                    val progress = workInfo.progress.getInt("progress", 0)

                    currentStatus = when (stage) {
                        "Сжатие" -> "Сжимаем фото..."
                        "Водяной знак" -> "Добавляем водяной знак..."
                        "Загрузка" -> "Загружаем в облако..."
                        else -> currentStatus
                    }
                    currentProgress = progress
                }
                WorkInfo.State.SUCCEEDED -> {
                    if (workInfo.tags.contains("upload")) {
                        isProcessing = false
                        val fileName = workInfo.outputData.getString("file_name") ?: ""
                        resultMessage = "Готово! Фото загружено\n" +
                                "Файл: $fileName\n"
                        currentProgress = 100
                        currentStatus = "Завершено!"
                    }
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Обработка фото",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(
                text = currentStatus,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isProcessing) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { currentProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )


                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$currentProgress%",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                startPhotoProcessing(context, workManager)
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isProcessing) "Обработка..." else "Начать обработку и загрузку")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (resultMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = resultMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

private fun startPhotoProcessing(
    context: android.content.Context,
    workManager: WorkManager
) {
    val testPhotoFile = File(context.cacheDir, "test_photo.jpg")
    testPhotoFile.writeText("fake image content")


    val compressionWorker = OneTimeWorkRequestBuilder<PhotoCompressionWorker>()
        .setInputData(
            Data.Builder()
                .putString("photo_path", testPhotoFile.absolutePath)
                .build()
        )
        .addTag("compression")
        .build()

    val watermarkWorker = OneTimeWorkRequestBuilder<WatermarkWorker>()
        .addTag("watermark")
        .build()

    val uploadWorker = OneTimeWorkRequestBuilder<UploadWorker>()
        .addTag("upload")
        .build()

    workManager
        .beginUniqueWork(
            "photo_processing",
            ExistingWorkPolicy.REPLACE,
            compressionWorker
        )
        .then(watermarkWorker)
        .then(uploadWorker)
        .enqueue()
}