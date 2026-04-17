package com.example.tetris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.tetris.ui.SplashScreen
import com.example.tetris.ui.TetrisScreen
import com.example.tetris.ui.theme.TetrisTheme
import com.example.tetris.viewmodel.TetrisViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TetrisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включаем отрисовку под системными панелями (Edge-to-Edge)
        enableEdgeToEdge()

        // Включаем Immersive Mode (скрываем статус-бар и навбар)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            TetrisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Состояние, определяющее, какой экран показывать
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        // Показываем сплеш-экран. Когда он закончит анимацию, меняем флаг
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        // Запускаем игру
                        TetrisScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}