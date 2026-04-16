package com.example.tetris.model

import androidx.compose.ui.graphics.Color

data class ThemeColors(
    val bg: Color,
    val board: Color,
    val grid: Color,
    val ghost: Color,
    val text: Color,
    val i: Color, val o: Color, val t: Color, val s: Color, val z: Color, val j: Color, val l: Color
) {
    fun getPieceColor(tetromino: Tetromino): Color = when (tetromino) {
        Tetromino.I -> i; Tetromino.O -> o; Tetromino.T -> t
        Tetromino.S -> s; Tetromino.Z -> z; Tetromino.J -> j; Tetromino.L -> l
    }
}

object ThemeConfig {
    val classic = ThemeColors(
        bg = Color(0xFF2C3E50), board = Color(0xCC000000), grid = Color(0xFF34495E), ghost = Color(0x33FFFFFF), text = Color.White,
        i = Color(0xFF00F0F0), o = Color(0xFFF0F000), t = Color(0xFFA000F0), s = Color(0xFF00F000), z = Color(0xFFF00000), j = Color(0xFF0000F0), l = Color(0xFFF0A000)
    )
    val neon = ThemeColors(
        bg = Color(0xFF000000), board = Color(0xE6000000), grid = Color(0xFF111111), ghost = Color(0x33FFFFFF), text = Color.White,
        i = Color(0xFF00FFFF), o = Color(0xFFFFFF00), t = Color(0xFFFF00FF), s = Color(0xFF00FF00), z = Color(0xFFFF0000), j = Color(0xFF0066FF), l = Color(0xFFFF6600)
    )
    val retro = ThemeColors(
        bg = Color(0xFFFFFFFF), board = Color(0xFFFFFFFF), grid = Color(0xFFEEEEEE), ghost = Color(0x1A000000), text = Color.Black,
        i = Color(0xFF00FFFF), o = Color(0xFFFFFF00), t = Color(0xFFFF00FF), s = Color(0xFF00FF00), z = Color(0xFFFF0000), j = Color(0xFF0000FF), l = Color(0xFFFF8800)
    )
    val minimal = ThemeColors(
        bg = Color(0xFFFFFFFF), board = Color(0xFFF0F0F0), grid = Color(0xFFE0E0E0), ghost = Color(0x1A000000), text = Color.Black,
        i = Color(0xFF95A5A6), o = Color(0xFF7F8C8D), t = Color(0xFF34495E), s = Color(0xFF2C3E50), z = Color(0xFF2980B9), j = Color(0xFF3498DB), l = Color(0xFF1ABC9C)
    )
    val nyancat = ThemeColors(
        bg = Color(0xFF000000), board = Color(0x80000000), grid = Color(0x1AFFFFFF), ghost = Color(0x33FF69B4), text = Color.White,
        i = Color(0xFFFF0000), o = Color(0xFFFFA500), t = Color(0xFFFFFF00), s = Color(0xFF00FF00), z = Color(0xFF0000FF), j = Color(0xFF4B0082), l = Color(0xFFFF00FF)
    )

    fun getColors(theme: AppTheme): ThemeColors = when (theme) {
        AppTheme.CLASSIC -> classic
        AppTheme.NEON -> neon
        AppTheme.RETRO -> retro
        AppTheme.MINIMAL -> minimal
        AppTheme.NYANCAT -> nyancat
    }
}