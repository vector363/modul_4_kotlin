package com.example.modul_4_pract_1_4

import android.os.Build
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.modul_4_pract_1_4.ui.theme.Modul_4_pract_14Theme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        setContent {
            Modul_4_pract_14Theme {
                AnimalFactScreen()
            }
        }
    }
}

@Composable
fun AnimalFactScreen(
    viewModel: AnimalFactViewModel = viewModel()
) {
    val isProcessing = viewModel.isLoading
    val animalFact = viewModel.currentFact

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Факты о животных",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Gray
                )
            ) {
                Text(
                    text = if (animalFact.isNotEmpty()) animalFact else "Нажмите на кнопку чтобы узнать факт",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.loadNewFact() },
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        ) {
            Text(
                text = if (isProcessing) "Загрузка..." else "Новый факт!",
                fontSize = 18.sp
            )
        }
    }
}



