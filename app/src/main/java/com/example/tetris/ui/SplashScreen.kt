package com.example.tetris.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tetris.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    // Анимация заполнения прогресса
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            progress = value
        }
        delay(200)
        onSplashFinished()
    }

    // Бесконечная анимация пульсации для последнего кубика
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulsing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, // Минимальная яркость
        targetValue = 1f,    // Максимальная яркость
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlphaAnim"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {

        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Splash Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .fillMaxWidth(0.65f)
                .height(18.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val blockGap = 6.dp.toPx() // Уменьшили отступы
                val blockSize = size.height * 0.7f // Кубики занимают 70% высоты трека

                // Рассчитываем количество кубиков
                val totalBlocks = (size.width / (blockSize + blockGap)).toInt()
                // Сколько кубиков уже заполнено
                val activeBlocks = (totalBlocks * progress).toInt()

                // Отрисовка всех ячеек
                for (i in 0 until totalBlocks) {
                    val blockX = i * (blockSize + blockGap)
                    val blockY = (size.height - blockSize) / 2f

                    if (i < activeBlocks) {
                        // активный блок
                        val colorRatio = i.toFloat() / totalBlocks.toFloat()
                        val baseColor = Color(
                            red = 1f - colorRatio,
                            green = colorRatio,
                            blue = 1f
                        )

                        // тень
                        drawRoundRect(
                            color = baseColor.copy(alpha = 0.3f),
                            topLeft = Offset(blockX, blockY),
                            size = Size(blockSize, blockSize),
                            cornerRadius = CornerRadius(3f, 3f),
                            style = Stroke(width = 6f)
                        )

                        // кубик
                        drawRoundRect(
                            color = baseColor,
                            topLeft = Offset(blockX, blockY),
                            size = Size(blockSize, blockSize),
                            cornerRadius = CornerRadius(3f, 3f)
                        )

                        if (i == activeBlocks - 1) {
                            drawRoundRect(
                                color = Color(0xFF00FFFF).copy(alpha = pulseAlpha * 0.5f), // Циан
                                topLeft = Offset(blockX - 2.dp.toPx(), blockY - 2.dp.toPx()), // Вылезает за края
                                size = Size(blockSize + 4.dp.toPx(), blockSize + 4.dp.toPx()),
                                cornerRadius = CornerRadius(5f, 5f),
                                style = Stroke(width = 10f)
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = pulseAlpha * 0.8f),
                                topLeft = Offset(blockX + blockSize/4, blockY + blockSize/4),
                                size = Size(blockSize/2, blockSize/2),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }

                    } else {
                        // подложка
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.15f),
                            topLeft = Offset(blockX, blockY),
                            size = Size(blockSize, blockSize),
                            cornerRadius = CornerRadius(3f, 3f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
        }
    }
}