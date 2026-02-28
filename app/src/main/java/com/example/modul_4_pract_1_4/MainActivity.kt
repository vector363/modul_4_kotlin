package com.example.modul_4_pract_1_4

import android.content.Context
import android.hardware.Sensor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_4_pract_14Theme {
                CompassApp()
            }
        }
    }
}

@Composable
fun CompassApp() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    var hasCompass by remember { mutableStateOf(accelerometer != null && magnetometer != null) }
    var azimuth by remember { mutableStateOf(0f) }
    var isSensorAvailable by remember { mutableStateOf(true) }

    val rotation = remember { Animatable(0f) }

    val lowPassFilter = remember { LowPassFilter() }

    DisposableEffect(Unit) {
        if (!hasCompass) {
            isSensorAvailable = false
            return@DisposableEffect onDispose { }
        }

        val listener = object : SensorEventListener {
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        gravity[0] = event.values[0]
                        gravity[1] = event.values[1]
                        gravity[2] = event.values[2]
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        geomagnetic[0] = event.values[0]
                        geomagnetic[1] = event.values[1]
                        geomagnetic[2] = event.values[2]
                    }
                }

                val rotationMatrix = FloatArray(9)
                if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    var newAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    newAzimuth = (newAzimuth + 360) % 360

                    val filteredAzimuth = lowPassFilter.apply(azimuth, newAzimuth)

                    azimuth = filteredAzimuth
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            listener,
            magnetometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LaunchedEffect(azimuth) {
        rotation.animateTo(
            targetValue = -azimuth,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Компас",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${azimuth.toInt()}°",
            fontSize = 20.sp,
            color = Color.White
        )

        Text(
            text = getDirectionName(azimuth),
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        CompassView(
            rotation = azimuth,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
        )
    }
}

// низкочастотный фильтр для сглаживания показаний
class LowPassFilter {
    private var filteredValue = 0f
    private val alpha = 0.2f
    private var initialized = false

    fun apply(current: Float, new: Float): Float {
        if (!initialized) {
            filteredValue = new
            initialized = true
            return new
        }

        var newValue = new
        if (Math.abs(newValue - filteredValue) > 180) {
            if (newValue > filteredValue) {
                filteredValue += 360
            } else {
                newValue += 360
            }
        }

        filteredValue = alpha * newValue + (1 - alpha) * filteredValue

        return filteredValue % 360
    }
}

@Composable
fun getDirectionName(azimuth: Float): String {
    return when {
        azimuth < 22.5 || azimuth >= 337.5 -> "Север"
        azimuth < 67.5 -> "Северо-восток"
        azimuth < 112.5 -> "Восток"
        azimuth < 157.5 -> "Юго-восток"
        azimuth < 202.5 -> "Юг"
        azimuth < 247.5 -> "Юго-запад"
        azimuth < 292.5 -> "Запад"
        else -> "Северо-запад"
    }
}


@Composable
fun CompassView(
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.9f

        drawCircle(
            color = Color.DarkGray,
            radius = radius,
            center = Offset(centerX, centerY)
        )

        drawRotatedArrow(centerX, centerY, radius * 0.7f, rotation)
    }
}

private fun DrawScope.drawRotatedArrow(centerX: Float, centerY: Float, length: Float, azimuth: Float) {
    val rad = Math.toRadians(azimuth.toDouble()).toFloat()

    val northX = centerX + length * sin(rad)
    val northY = centerY - length * cos(rad)

    val southX = centerX - length * sin(rad)
    val southY = centerY + length * cos(rad)

    drawLine(
        color = Color.Red,
        start = Offset(centerX, centerY),
        end = Offset(northX, northY),
        strokeWidth = 8.dp.toPx()
    )

    drawLine(
        color = Color.Gray,
        start = Offset(centerX, centerY),
        end = Offset(southX, southY),
        strokeWidth = 8.dp.toPx()
    )

    drawCircle(
        color = Color.White,
        radius = 6.dp.toPx(),
        center = Offset(centerX, centerY)
    )
}


