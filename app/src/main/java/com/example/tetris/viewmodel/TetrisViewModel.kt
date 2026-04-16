package com.example.tetris.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetris.audio.SoundManager
import com.example.tetris.data.SettingsStore
import com.example.tetris.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TetrisViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var currentBag = mutableListOf<Tetromino>()
    private val settingsStore = SettingsStore(application)
    private val soundManager = SoundManager(application)

    init {
        viewModelScope.launch {
            val savedScore = settingsStore.highScoreFlow.first()
            val unlockedIds = settingsStore.unlockedAchievementsFlow.first()
            val savedVolume = settingsStore.volumeFlow.first()
            val savedThemeStr = settingsStore.themeFlow.first()
            val savedTheme = runCatching { AppTheme.valueOf(savedThemeStr) }.getOrDefault(AppTheme.NEON)
            val savedLangStr = settingsStore.languageFlow.first()
            val savedLang = runCatching { AppLanguage.valueOf(savedLangStr) }.getOrDefault(AppLanguage.EN)

            soundManager.setVolume(savedVolume)
            val achievements = Locales.getAchievements(savedLang, unlockedIds)

            val firstPiece = popNextTetromino()
            val nextPiece = peekNextTetromino()

            _state.value = _state.value.copy(
                highScore = savedScore, achievements = achievements, volume = savedVolume,
                theme = savedTheme, language = savedLang, currentTetromino = firstPiece,
                nextTetromino = nextPiece, currentShape = firstPiece.shape, currentX = 6, currentY = 0
            )
            startGameLoop()
        }
    }

    private fun ensureBagFilled() {
        if (currentBag.size < 2) currentBag.addAll(Tetromino.entries.shuffled())
    }

    private fun popNextTetromino(): Tetromino {
        ensureBagFilled()
        return currentBag.removeAt(0)
    }

    private fun peekNextTetromino(): Tetromino {
        ensureBagFilled()
        return currentBag[0]
    }

    fun setVolume(vol: Float) {
        _state.value = _state.value.copy(volume = vol)
        soundManager.setVolume(vol)
        viewModelScope.launch { settingsStore.saveVolume(vol) }
    }

    fun setTheme(theme: AppTheme) {
        _state.value = _state.value.copy(theme = theme)
        if (_state.value.status == GameStatus.PLAYING) soundManager.playBackgroundMusic(theme)
        viewModelScope.launch { settingsStore.saveTheme(theme.name) }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            val unlockedIds = settingsStore.unlockedAchievementsFlow.first()
            _state.value = _state.value.copy(language = lang, achievements = Locales.getAchievements(lang, unlockedIds))
            settingsStore.saveLanguage(lang.name)
        }
    }

    fun startGame() {
        val firstPiece = popNextTetromino()
        val nextPiece = peekNextTetromino()
        _state.value = _state.value.copy(
            status = GameStatus.PLAYING, currentTetromino = firstPiece, nextTetromino = nextPiece,
            currentShape = firstPiece.shape, currentX = 6, currentY = 0, clearingLines = emptyList()
        )
        soundManager.playBackgroundMusic(_state.value.theme)
    }

    fun pauseGame() {
        if (_state.value.status == GameStatus.PLAYING) {
            _state.value = _state.value.copy(status = GameStatus.PAUSED)
            soundManager.pauseBackgroundMusic()
        }
    }

    fun resumeGame() {
        if (_state.value.status == GameStatus.PAUSED) {
            _state.value = _state.value.copy(status = GameStatus.PLAYING)
            soundManager.resumeBackgroundMusic(_state.value.theme)
        }
    }

    fun restartGame() {
        currentBag.clear()
        val firstPiece = popNextTetromino()
        val nextPiece = peekNextTetromino()
        _state.value = _state.value.copy(
            status = GameStatus.PLAYING, currentTetromino = firstPiece, nextTetromino = nextPiece,
            currentShape = firstPiece.shape, currentX = 6, currentY = 0, score = 0, level = 1,
            linesCleared = 0, hardDropsCount = 0, piecesPlaced = 0, rotationsCount = 0,
            notificationAchievement = null, grid = List(24) { List(16) { null } }, clearingLines = emptyList()
        )
        soundManager.playBackgroundMusic(_state.value.theme)
    }

    fun clearNotification() { _state.value = _state.value.copy(notificationAchievement = null) }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (isActive) {
                val delayTime = maxOf(100L, 500L - (_state.value.level - 1) * 40L)
                delay(delayTime)
                if (_state.value.status == GameStatus.PLAYING && !_state.value.isHardDropping && _state.value.clearingLines.isEmpty()) {
                    moveDown()
                }
            }
        }
    }

    fun moveLeft() {
        val s = _state.value
        if (s.status != GameStatus.PLAYING || s.isHardDropping || s.clearingLines.isNotEmpty()) return
        if (isValidMove(s.currentShape, s.currentX - 1, s.currentY)) _state.value = s.copy(currentX = s.currentX - 1)
    }

    fun moveRight() {
        val s = _state.value
        if (s.status != GameStatus.PLAYING || s.isHardDropping || s.clearingLines.isNotEmpty()) return
        if (isValidMove(s.currentShape, s.currentX + 1, s.currentY)) _state.value = s.copy(currentX = s.currentX + 1)
    }

    fun rotate() {
        val s = _state.value
        if (s.status != GameStatus.PLAYING || s.isHardDropping || s.clearingLines.isNotEmpty()) return
        val shape = s.currentShape
        val rotatedShape = Array(shape[0].size) { i -> IntArray(shape.size) { j -> shape[shape.size - 1 - j][i] } }
        if (isValidMove(rotatedShape, s.currentX, s.currentY)) {
            _state.value = s.copy(currentShape = rotatedShape, rotationsCount = s.rotationsCount + 1)
        }
    }

    fun getGhostY(): Int {
        val s = _state.value
        var dropY = s.currentY
        while (isValidMove(s.currentShape, s.currentX, dropY + 1)) dropY++
        return dropY
    }

    fun hardDrop() {
        val s = _state.value
        if (s.status != GameStatus.PLAYING || s.isHardDropping || s.clearingLines.isNotEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isHardDropping = true, hardDropsCount = _state.value.hardDropsCount + 1)
            while (isValidMove(_state.value.currentShape, _state.value.currentX, _state.value.currentY + 1)) {
                _state.value = _state.value.copy(currentY = _state.value.currentY + 1)
                delay(25)
            }
            lockTetromino()
            _state.value = _state.value.copy(isHardDropping = false)
        }
    }

    private fun moveDown() {
        val s = _state.value
        if (isValidMove(s.currentShape, s.currentX, s.currentY + 1)) _state.value = s.copy(currentY = s.currentY + 1) else lockTetromino()
    }

    private fun lockTetromino() {
        val s = _state.value
        val newGrid = s.grid.map { it.toMutableList() }.toMutableList()

        s.currentShape.forEachIndexed { y, row ->
            row.forEachIndexed { x, value ->
                if (value == 1 && s.currentY + y in 0..23 && s.currentX + x in 0..15) {
                    newGrid[s.currentY + y][s.currentX + x] = s.currentTetromino
                }
            }
        }

        val linesToClear = mutableListOf<Int>()
        for (i in newGrid.indices) {
            if (newGrid[i].all { it != null }) {
                linesToClear.add(i)
            }
        }

        if (linesToClear.isNotEmpty()) {
            _state.value = s.copy(
                grid = newGrid,
                clearingLines = linesToClear,
                currentY = -10,
                isHardDropping = false
            )

            viewModelScope.launch {
                delay(300)

                val finalGrid = newGrid.filterIndexed { index, _ -> index !in linesToClear }.toMutableList()
                repeat(linesToClear.size) { finalGrid.add(0, MutableList<Tetromino?>(16) { null }) }

                val clearedInThisTurn = linesToClear.size
                val isPerfectClear = finalGrid.all { row -> row.all { it == null } }
                val scoreMultiplier = when (clearedInThisTurn) { 1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0 }
                val newScore = s.score + (scoreMultiplier * s.level)
                val newLevel = (newScore / 1000) + 1
                val newLinesTotal = s.linesCleared + clearedInThisTurn

                val newHighScore = maxOf(s.highScore, newScore)
                if (newHighScore > s.highScore) settingsStore.saveHighScore(newHighScore)

                val nextTetromino = popNextTetromino()
                val upcomingTetromino = peekNextTetromino()

                var tempState = _state.value.copy(
                    grid = finalGrid,
                    clearingLines = emptyList(),
                    currentTetromino = nextTetromino,
                    nextTetromino = upcomingTetromino,
                    currentShape = nextTetromino.shape,
                    currentX = 6,
                    currentY = 0,
                    score = newScore,
                    highScore = newHighScore,
                    linesCleared = newLinesTotal,
                    level = newLevel,
                    piecesPlaced = s.piecesPlaced + 1
                )

                val (newAchievements, newNotification) = checkAchievements(tempState, clearedInThisTurn, isPerfectClear)
                tempState = tempState.copy(achievements = newAchievements, notificationAchievement = newNotification ?: tempState.notificationAchievement)

                if (!isValidMove(nextTetromino.shape, 6, 0)) {
                    tempState = tempState.copy(status = GameStatus.GAME_OVER)
                    soundManager.stopBackgroundMusic()
                    soundManager.playGameOver()
                }
                _state.value = tempState
            }
        } else {
            val isPerfectClear = newGrid.all { row -> row.all { it == null } }
            val nextTetromino = popNextTetromino()
            val upcomingTetromino = peekNextTetromino()

            var tempState = s.copy(
                grid = newGrid,
                currentTetromino = nextTetromino,
                nextTetromino = upcomingTetromino,
                currentShape = nextTetromino.shape,
                currentX = 6,
                currentY = 0,
                piecesPlaced = s.piecesPlaced + 1,
                isHardDropping = false
            )

            val (newAchievements, newNotification) = checkAchievements(tempState, 0, isPerfectClear)
            tempState = tempState.copy(achievements = newAchievements, notificationAchievement = newNotification ?: tempState.notificationAchievement)

            if (!isValidMove(nextTetromino.shape, 6, 0)) {
                tempState = tempState.copy(status = GameStatus.GAME_OVER)
                soundManager.stopBackgroundMusic()
                soundManager.playGameOver()
            }
            _state.value = tempState
        }
    }

    private fun checkAchievements(s: GameState, cleared: Int, perfect: Boolean): Pair<List<Achievement>, Achievement?> {
        val list = s.achievements.toMutableList()
        var newlyUnlocked: Achievement? = null
        fun unlock(index: Int) {
            if (!list[index].isUnlocked) {
                list[index] = list[index].copy(isUnlocked = true)
                viewModelScope.launch { settingsStore.saveUnlockedAchievement(list[index].id) }
                if (newlyUnlocked == null) newlyUnlocked = list[index]
            }
        }
        if (s.linesCleared >= 1) unlock(0)
        if (s.linesCleared >= 10) unlock(1)
        if (s.linesCleared >= 50) unlock(2)
        if (s.linesCleared >= 100) unlock(3)
        if (s.level >= 2) unlock(4)
        if (s.level >= 5) unlock(5)
        if (s.level >= 10) unlock(6)
        if (s.level >= 15) unlock(7)
        if (s.score >= 1000) unlock(8)
        if (s.score >= 10000) unlock(9)
        if (s.score >= 50000) unlock(10)
        if (s.score >= 100000) unlock(11)
        if (cleared == 2) unlock(12)
        if (cleared == 3) unlock(13)
        if (cleared == 4) unlock(14)
        if (s.hardDropsCount >= 10) unlock(15)
        if (s.piecesPlaced >= 50) unlock(16)
        if (s.piecesPlaced >= 200) unlock(17)
        if (perfect) unlock(18)
        if (s.rotationsCount >= 100) unlock(19)
        return Pair(list, newlyUnlocked)
    }

    private fun isValidMove(shape: Array<IntArray>, nextX: Int, nextY: Int): Boolean {
        val grid = _state.value.grid
        shape.forEachIndexed { y, row ->
            row.forEachIndexed { x, value ->
                if (value == 1) {
                    val gridX = nextX + x; val gridY = nextY + y
                    if (gridX !in 0..<16 || gridY >= 24) return false
                    if (gridY >= 0 && grid[gridY][gridX] != null) return false
                }
            }
        }
        return true
    }
}