package com.example.tetris.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tetris.model.*
import com.example.tetris.viewmodel.TetrisViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

fun Color.shade(percent: Float): Color {
    val r = (this.red + percent).coerceIn(0f, 1f)
    val g = (this.green + percent).coerceIn(0f, 1f)
    val b = (this.blue + percent).coerceIn(0f, 1f)
    return Color(r, g, b, this.alpha)
}

@Composable
fun AnimatedCircleButton(icon: ImageVector, containerColor: Color, iconColor: Color = Color.White, size: Int = 64, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.75f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "btn")
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp).scale(scale).clip(CircleShape).background(containerColor).pointerInput(Unit) {
        detectTapGestures(onPress = { isPressed = true; val released = tryAwaitRelease(); isPressed = false; if (released) onClick() })
    }) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size((size / 2).dp)) }
}

@Composable
fun AchievementNotification(achievement: Achievement?, lang: AppLanguage, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        AnimatedVisibility(visible = achievement != null, enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()) {
            achievement?.let { ach ->
                Row(modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFF333333)).padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(Locales.t("ach_unlocked", lang), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(ach.title, color = Color(0xFFFFD700), fontSize = 12.sp)
                    }
                }
                LaunchedEffect(ach) { delay(3000); onClear() }
            }
        }
    }
}

fun DrawScope.drawNyanCatBackground(startX: Float, startY: Float, width: Float, height: Float) {
    clipRect(left = startX, top = startY, right = startX + width, bottom = startY + height) {
        val skyGradient = Brush.verticalGradient(listOf(Color(0xFF8B5E9C), Color(0xFFE37B96), Color(0xFFFFB6C1)), startY = startY, endY = startY + height)
        drawRect(skyGradient, topLeft = Offset(startX, startY), size = Size(width, height))

        val seaGradient = Brush.verticalGradient(listOf(Color(0xFF4FA4E4), Color(0xFF1E90FF)), startY = startY + height * 0.8f, endY = startY + height)
        drawRect(seaGradient, topLeft = Offset(startX, startY + height * 0.8f), size = Size(width, height * 0.2f))

        drawCircle(Color(0xFFFFE135), radius = width * 0.15f, center = Offset(startX + width * 0.2f, startY + height * 0.3f))

        val rainbowColors = listOf(Color(0xFFFF0000), Color(0xFFFF9900), Color(0xFFFFFF00), Color(0xFF33FF00), Color(0xFF0099FF), Color(0xFF6633FF))
        val stripeHeight = height * 0.05f
        rainbowColors.forEachIndexed { i, color -> drawRect(color, topLeft = Offset(startX, startY + height * 0.4f + i * stripeHeight), size = Size(width * 0.3f, stripeHeight)) }

        val stars = listOf(Offset(startX + width * 0.8f, startY + height * 0.1f), Offset(startX + width * 0.9f, startY + height * 0.2f), Offset(startX + width * 0.7f, startY + height * 0.15f))
        stars.forEach { star ->
            drawRect(Color.White, topLeft = Offset(star.x - 2f, star.y - 8f), size = Size(4f, 16f))
            drawRect(Color.White, topLeft = Offset(star.x - 8f, star.y - 2f), size = Size(16f, 4f))
        }
    }
}

@Composable
fun TetrisScreen(viewModel: TetrisViewModel) {
    val state by viewModel.state.collectAsState()
    val ghostY = viewModel.getGhostY()

    var showMenu by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition()
    val timeMillis by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
    )

    val isClearing = state.clearingLines.isNotEmpty()
    val clearAnimProgress by animateFloatAsState(
        targetValue = if (isClearing) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "clearAnim"
    )

    val themeColors = ThemeConfig.getColors(state.theme)

    LaunchedEffect(state.clearingLines) {
        if (state.clearingLines.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(150)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(state.piecesPlaced) {
        if (state.piecesPlaced > 0 && !state.isHardDropping && state.clearingLines.isEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(themeColors.bg)) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text(Locales.t("score", state.language), color = Color.Gray, fontSize = 12.sp); Text("${state.score}", color = themeColors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(Locales.t("level", state.language), color = Color.Gray, fontSize = 12.sp); Text("${state.level}", color = Color(0xFF00BFFF), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.End) { Text(Locales.t("record", state.language), color = Color.Gray, fontSize = 12.sp); Text("${state.highScore}", color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()
                .pointerInput(Unit) {
                    var dragAccX = 0f; var isDropped = false
                    detectDragGestures(onDragStart = { dragAccX = 0f; isDropped = false }, onDragEnd = { dragAccX = 0f; isDropped = false }) { change, dragAmount ->
                        change.consume()
                        if (state.status != GameStatus.PLAYING || state.isHardDropping || isClearing) return@detectDragGestures

                        if (dragAmount.y > 30 && dragAmount.y > abs(dragAmount.x) && !isDropped) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.hardDrop()
                            isDropped = true
                        }
                        else if (!isDropped) {
                            dragAccX += dragAmount.x
                            if (dragAccX > 50f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.moveRight(); dragAccX = 0f
                            } else if (dragAccX < -50f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.moveLeft(); dragAccX = 0f
                            }
                        }
                    }
                }.pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (state.status == GameStatus.PLAYING && !state.isHardDropping && !isClearing) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.rotate()
                        }
                    })
                }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cols = 16
                    val rows = 24
                    val cellSize = minOf(size.width / cols, size.height / rows)
                    val boardWidth = cellSize * cols
                    val boardHeight = cellSize * rows
                    val startX = (size.width - boardWidth) / 2f
                    val startY = (size.height - boardHeight) / 2f

                    if (state.theme == AppTheme.NYANCAT) {
                        drawNyanCatBackground(startX, startY, boardWidth, boardHeight)
                    }

                    val totalCols = (size.width / cellSize).toInt() + 2
                    val totalRows = (size.height / cellSize).toInt() + 2
                    val bgStartX = (size.width - totalCols * cellSize) / 2f
                    val bgStartY = (size.height - totalRows * cellSize) / 2f

                    if (state.theme != AppTheme.NYANCAT) {
                        for (i in 0..totalCols) { drawLine(themeColors.grid.copy(alpha = 0.2f), Offset(bgStartX + i * cellSize, 0f), Offset(bgStartX + i * cellSize, size.height)) }
                        for (i in 0..totalRows) { drawLine(themeColors.grid.copy(alpha = 0.2f), Offset(0f, bgStartY + i * cellSize), Offset(size.width, bgStartY + i * cellSize)) }

                        drawRect(themeColors.board, Offset(startX, startY), Size(boardWidth, boardHeight))
                    }

                    for (i in 0..cols) drawLine(themeColors.grid, Offset(startX + i * cellSize, startY), Offset(startX + i * cellSize, startY + boardHeight))
                    for (i in 0..rows) drawLine(themeColors.grid, Offset(startX, startY + i * cellSize), Offset(startX + boardWidth, startY + i * cellSize))
                    drawRect(themeColors.grid, Offset(startX, startY), Size(boardWidth, boardHeight), style = Stroke(4f))

                    fun drawStyledBlock(absX: Float, absY: Float, cSize: Float, color: Color, alpha: Float = 1f, isGhost: Boolean = false) {
                        val s = cSize - 2f
                        when (state.theme) {
                            AppTheme.RETRO -> {
                                if (isGhost) {
                                    drawRect(Color.Black.copy(alpha = 0.1f), Offset(absX + 1f, absY + 1f), Size(s, s))
                                } else {
                                    val shadowSize = cSize * 0.15f
                                    drawRect(color.shade(-0.04f).copy(alpha = alpha), Offset(absX + 1f, absY + 1f), Size(s, s))
                                    drawRect(color.shade(0.15f).copy(alpha = alpha), Offset(absX + 1f, absY + 1f), Size(s, shadowSize))
                                    drawRect(color.shade(-0.08f).copy(alpha = alpha), Offset(absX + 1f + s - shadowSize, absY + 1f), Size(shadowSize, s))
                                    drawRect(color.shade(-0.12f).copy(alpha = alpha), Offset(absX + 1f, absY + 1f + s - shadowSize), Size(s, shadowSize))
                                    drawRect(color.shade(-0.06f).copy(alpha = alpha), Offset(absX + 1f, absY + 1f), Size(shadowSize, s))
                                }
                            }
                            AppTheme.NEON -> {
                                val drawColor = if (isGhost) {
                                    val discoColors = listOf(Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFFFF00), Color(0xFF00FF00))
                                    val index = ((timeMillis / 200f) % discoColors.size).toInt()
                                    discoColors[index].copy(alpha = 0.3f)
                                } else color.copy(alpha = alpha)

                                drawRect(drawColor, Offset(absX + 1f, absY + 1f), Size(s, s))
                                drawRect(Color.White.copy(alpha = if (isGhost) 0.5f else alpha), Offset(absX + 1f, absY + 1f), Size(s, s), style = Stroke(2f))
                            }
                            else -> {
                                drawRect(color.copy(alpha = if (isGhost) 0.2f else alpha), Offset(absX + 1f, absY + 1f), Size(s, s))
                                if (!isGhost && state.theme != AppTheme.NYANCAT && state.theme != AppTheme.MINIMAL) {
                                    drawRect(Color.Black, Offset(absX, absY), Size(cSize, cSize), style = Stroke(2f))
                                }
                            }
                        }
                    }

                    state.grid.forEachIndexed { y, row ->
                        row.forEachIndexed { x, tetromino ->
                            if (tetromino != null) {
                                drawStyledBlock(startX + x * cellSize, startY + y * cellSize, cellSize, themeColors.getPieceColor(tetromino))

                                if (y in state.clearingLines) {
                                    drawRect(Color.White.copy(alpha = 0.6f), Offset(startX + x * cellSize, startY + y * cellSize), Size(cellSize, cellSize))
                                }
                            }
                        }
                    }

                    val currentPieceColor = themeColors.getPieceColor(state.currentTetromino)

                    if (!isClearing) {
                        if (!state.isHardDropping && state.status == GameStatus.PLAYING) {
                            state.currentShape.forEachIndexed { y, row ->
                                row.forEachIndexed { x, value ->
                                    if (value == 1) drawStyledBlock(startX + (state.currentX + x) * cellSize, startY + (ghostY + y) * cellSize, cellSize, currentPieceColor, isGhost = true)
                                }
                            }
                        }

                        if (state.isHardDropping) {
                            val tailLength = 8 * cellSize
                            state.currentShape.forEachIndexed { y, row ->
                                row.forEachIndexed { x, value ->
                                    if (value == 1) {
                                        val bX = startX + (state.currentX + x) * cellSize
                                        val pieceCurrentY = startY + (state.currentY + y) * cellSize
                                        val tailTopY = maxOf(startY, pieceCurrentY - tailLength)
                                        val height = pieceCurrentY - tailTopY

                                        if (height > 0) {
                                            when (state.theme) {
                                                AppTheme.NEON -> {
                                                    val laserColors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta, Color.Cyan, Color.Yellow)
                                                    val c = laserColors[((timeMillis / 100f) % laserColors.size).toInt()]
                                                    drawLine(c.copy(alpha = 0.6f), Offset(bX + cellSize/2, tailTopY), Offset(bX + cellSize/2, pieceCurrentY), strokeWidth = 4f)
                                                }
                                                AppTheme.NYANCAT -> {
                                                    val gradientColors = listOf(Color.Red, Color(0xFFFFA500), Color.Yellow, Color.Green, Color.Blue, Color(0xFF4B0082))
                                                    drawRect(Brush.verticalGradient(gradientColors, startY = tailTopY, endY = pieceCurrentY), Offset(bX, tailTopY), Size(cellSize, height), alpha = 0.5f)
                                                }
                                                AppTheme.RETRO -> {
                                                    val pixelSize = 6f
                                                    for (py in tailTopY.toInt()..pieceCurrentY.toInt() step pixelSize.toInt() * 2) {
                                                        for (px in bX.toInt()..(bX + cellSize).toInt() step pixelSize.toInt() * 2) {
                                                            drawRect(Color.Black.copy(alpha = 0.1f), Offset(px.toFloat(), py.toFloat()), Size(pixelSize, pixelSize))
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, currentPieceColor.copy(alpha = 0.5f)), startY = tailTopY, endY = pieceCurrentY), Offset(bX, tailTopY), Size(cellSize, height))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (state.status == GameStatus.PLAYING || state.status == GameStatus.PAUSED) {
                            state.currentShape.forEachIndexed { y, row ->
                                row.forEachIndexed { x, value ->
                                    if (value == 1) drawStyledBlock(startX + (state.currentX + x) * cellSize, startY + (state.currentY + y) * cellSize, cellSize, currentPieceColor)
                                }
                            }
                        }
                    }

                    if (state.status == GameStatus.PLAYING || state.status == GameStatus.PAUSED) {
                        val previewCellSize = cellSize * 0.55f
                        val previewPadding = cellSize * 0.5f
                        val previewBoxWidth = previewCellSize * 4f + previewPadding * 2
                        val previewBoxHeight = previewCellSize * 4f + previewPadding * 2

                        val previewX = startX + boardWidth - previewBoxWidth - previewPadding
                        val previewY = startY + previewPadding

                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset(previewX, previewY),
                            size = Size(previewBoxWidth, previewBoxHeight),
                            cornerRadius = CornerRadius(12f, 12f)
                        )

                        val nextColor = themeColors.getPieceColor(state.nextTetromino)
                        val nextShape = state.nextTetromino.shape
                        val pieceW = nextShape[0].size * previewCellSize
                        val pieceH = nextShape.size * previewCellSize

                        val pieceStartX = previewX + (previewBoxWidth - pieceW) / 2f
                        val pieceStartY = previewY + (previewBoxHeight - pieceH) / 2f

                        nextShape.forEachIndexed { y, row ->
                            row.forEachIndexed { x, value ->
                                if (value == 1) {
                                    drawStyledBlock(pieceStartX + x * previewCellSize, pieceStartY + y * previewCellSize, previewCellSize, nextColor)
                                }
                            }
                        }
                    }

                    if (state.status != GameStatus.PLAYING) {
                        drawRect(Color.Black.copy(alpha = 0.7f), Offset(startX, startY), Size(boardWidth, boardHeight))
                    }
                }

                if (state.status != GameStatus.PLAYING) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val overlayText = when (state.status) {
                            GameStatus.START_SCREEN -> Locales.t("start_game", state.language)
                            GameStatus.PAUSED -> Locales.t("pause", state.language)
                            GameStatus.GAME_OVER -> Locales.t("game_over", state.language)
                            else -> ""
                        }
                        Text(overlayText, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp)) {
                Row(modifier = Modifier.align(Alignment.CenterStart)) {
                    AnimatedCircleButton(Icons.Rounded.WorkspacePremium, Color.Transparent, Color(0xFFFFD700), 60) { showAchievements = true }
                }

                Row(modifier = Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (state.status) {
                        GameStatus.START_SCREEN -> AnimatedCircleButton(Icons.Filled.PlayArrow, Color(0xFF00C853)) { viewModel.startGame() }
                        GameStatus.PLAYING -> AnimatedCircleButton(Icons.Filled.Pause, Color(0xFF424242)) { viewModel.pauseGame() }
                        GameStatus.PAUSED, GameStatus.GAME_OVER -> {
                            if (state.status == GameStatus.PAUSED) AnimatedCircleButton(Icons.Filled.PlayArrow, Color(0xFF00C853)) { viewModel.resumeGame() }
                            AnimatedCircleButton(Icons.Filled.Refresh, Color(0xFFD50000)) { viewModel.restartGame() }
                        }
                    }
                }

                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    AnimatedCircleButton(Icons.Filled.Settings, Color.Transparent, themeColors.text, 60) { showMenu = true }
                }
            }
        }

        AchievementNotification(state.notificationAchievement, state.language, { viewModel.clearNotification() }, Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(top = 16.dp))
    }

    if (showAchievements) {
        AlertDialog(onDismissRequest = { showAchievements = false }, title = { Text(Locales.t("achievements", state.language)) }, confirmButton = { TextButton(onClick = { showAchievements = false }) { Text(Locales.t("close", state.language)) } },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.achievements.size) { index ->
                        val ach = state.achievements[index]
                        val color = if (ach.isUnlocked) Color(0xFF4CAF50) else Color.DarkGray
                        val icon = if (ach.isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column { Text(ach.title, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(ach.description, color = Color.Gray, fontSize = 12.sp) }
                        }
                    }
                }
            }
        )
    }

    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(Locales.t("settings", state.language)) },
            confirmButton = { TextButton(onClick = { showMenu = false }) { Text(Locales.t("close", state.language)) } },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Column { Text("${Locales.t("volume", state.language)}: ${(state.volume * 100).toInt()}%"); Slider(value = state.volume, onValueChange = { viewModel.setVolume(it) }) }

                    Column {
                        Text(Locales.t("theme", state.language))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState())
                        ) {
                            AppTheme.values().forEach { t ->
                                val title = Locales.t("theme_${t.name.lowercase()}", state.language)
                                FilterChip(
                                    selected = state.theme == t,
                                    onClick = { viewModel.setTheme(t) },
                                    label = { Text(title) }
                                )
                            }
                        }
                    }

                    Column {
                        Text(Locales.t("lang", state.language))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top=8.dp)) {
                            FilterChip(selected = state.language == AppLanguage.EN, onClick = { viewModel.setLanguage(AppLanguage.EN) }, label = { Text("English") })
                            FilterChip(selected = state.language == AppLanguage.RU, onClick = { viewModel.setLanguage(AppLanguage.RU) }, label = { Text("Русский") })
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(Locales.t("how_to_play", state.language), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(Locales.t("rule_tap", state.language), fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.SwipeLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(Locales.t("rule_swipe_x", state.language), fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.SwipeDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(Locales.t("rule_swipe_y", state.language), fontSize = 14.sp)
                        }
                    }
                }
            }
        )
    }
}