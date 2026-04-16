package com.example.tetris.model

enum class GameStatus { START_SCREEN, PLAYING, PAUSED, GAME_OVER }
enum class AppTheme { NEON, NYANCAT, RETRO, CLASSIC, MINIMAL }
enum class AppLanguage { EN, RU }

data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false
)

data class GameState(
    val grid: List<List<Tetromino?>> = List(24) { List(16) { null } },
    val currentTetromino: Tetromino = Tetromino.T,
    val nextTetromino: Tetromino = Tetromino.I,
    val currentShape: Array<IntArray> = Tetromino.T.shape,
    val currentX: Int = 6,
    val currentY: Int = 0,

    val clearingLines: List<Int> = emptyList(),

    val score: Int = 0,
    val highScore: Int = 0,
    val level: Int = 1,
    val linesCleared: Int = 0,
    val status: GameStatus = GameStatus.START_SCREEN,
    val isHardDropping: Boolean = false,
    val notificationAchievement: Achievement? = null,

    val hardDropsCount: Int = 0,
    val piecesPlaced: Int = 0,
    val rotationsCount: Int = 0,

    val volume: Float = 1.0f,
    val theme: AppTheme = AppTheme.NEON,
    val language: AppLanguage = AppLanguage.EN,

    val achievements: List<Achievement> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GameState
        if (grid != other.grid) return false
        if (currentTetromino != other.currentTetromino) return false
        if (nextTetromino != other.nextTetromino) return false
        if (!currentShape.contentDeepEquals(other.currentShape)) return false
        if (currentX != other.currentX) return false
        if (currentY != other.currentY) return false
        if (clearingLines != other.clearingLines) return false
        if (score != other.score) return false
        if (highScore != other.highScore) return false
        if (level != other.level) return false
        if (linesCleared != other.linesCleared) return false
        if (status != other.status) return false
        if (isHardDropping != other.isHardDropping) return false
        if (notificationAchievement != other.notificationAchievement) return false
        if (hardDropsCount != other.hardDropsCount) return false
        if (piecesPlaced != other.piecesPlaced) return false
        if (rotationsCount != other.rotationsCount) return false
        if (volume != other.volume) return false
        if (theme != other.theme) return false
        if (language != other.language) return false
        if (achievements != other.achievements) return false
        return true
    }

    override fun hashCode(): Int {
        var result = grid.hashCode()
        result = 31 * result + currentTetromino.hashCode()
        result = 31 * result + nextTetromino.hashCode()
        result = 31 * result + currentShape.contentDeepHashCode()
        result = 31 * result + currentX
        result = 31 * result + currentY
        result = 31 * result + clearingLines.hashCode()
        result = 31 * result + score
        result = 31 * result + highScore
        result = 31 * result + level
        result = 31 * result + linesCleared
        result = 31 * result + status.hashCode()
        result = 31 * result + isHardDropping.hashCode()
        result = 31 * result + (notificationAchievement?.hashCode() ?: 0)
        result = 31 * result + hardDropsCount
        result = 31 * result + piecesPlaced
        result = 31 * result + rotationsCount
        result = 31 * result + volume.hashCode()
        result = 31 * result + theme.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + achievements.hashCode()
        return result
    }
}

object Locales {
    fun t(key: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.RU) ru[key] ?: key else en[key] ?: key
    }

    private val en = mapOf(
        "score" to "SCORE", "level" to "LEVEL", "record" to "RECORD", "lines" to "LINES",
        "achievements" to "Achievements", "settings" to "Settings", "close" to "Close",
        "start_game" to "START GAME", "pause" to "PAUSE", "game_over" to "GAME OVER",
        "ach_unlocked" to "Achievement Unlocked!", "volume" to "Volume",
        "theme" to "Theme", "theme_classic" to "Classic", "theme_neon" to "Neon",
        "theme_retro" to "Retro", "theme_minimal" to "Minimal", "theme_nyancat" to "Nyan Cat",
        "lang" to "Language",
        "how_to_play" to "How to Play",
        "rule_tap" to "Tap — Rotate piece",
        "rule_swipe_x" to "Swipe Left/Right — Move piece",
        "rule_swipe_y" to "Swipe Down — Hard drop"
    )

    private val ru = mapOf(
        "score" to "СЧЕТ", "level" to "УРОВЕНЬ", "record" to "РЕКОРД", "lines" to "ЛИНИИ",
        "achievements" to "Достижения", "settings" to "Настройки", "close" to "Закрыть",
        "start_game" to "НАЧАТЬ ИГРУ", "pause" to "ПАУЗА", "game_over" to "ИГРА ОКОНЧЕНА",
        "ach_unlocked" to "Достижение открыто!", "volume" to "Громкость",
        "theme" to "Тема", "theme_classic" to "Классика", "theme_neon" to "Неон",
        "theme_retro" to "Ретро", "theme_minimal" to "Минимализм", "theme_nyancat" to "Nyan Cat",
        "lang" to "Язык",
        "how_to_play" to "Управление",
        "rule_tap" to "Тап по экрану — Поворот",
        "rule_swipe_x" to "Свайп влево/вправо — Перемещение",
        "rule_swipe_y" to "Свайп вниз — Быстрый сброс"
    )

    fun getAchievements(lang: AppLanguage, unlockedIds: Set<String>): List<Achievement> {
        val titles = if (lang == AppLanguage.RU) listOf("Первая кровь", "Ученик", "Мастер", "Грандмастер", "Разминка", "Втягиваемся", "Киберспортсмен", "Скорость света", "Первые деньги", "Богач", "Олигарх", "Чемпион", "Двойной удар", "Тройная угроза", "ТЕТРИС!", "Метеоритный дождь", "Строитель", "Архитектор", "Идеальная чистота", "Любитель крутиться") else listOf("First Blood", "Apprentice", "Master", "Grandmaster", "Warm Up", "Getting Into It", "Esports Pro", "Speed of Light", "First Money", "Rich", "Oligarch", "Champion", "Double Strike", "Triple Threat", "TETRIS!", "Meteor Shower", "Builder", "Architect", "Perfect Clear", "Spin Lover")
        val descs = if (lang == AppLanguage.RU) listOf("Сожги 1 линию", "Сожги 10 линий", "Сожги 50 линий", "Сожги 100 линий", "Достигни 2 уровня", "Достигни 5 уровня", "Достигни 10 уровня", "Достигни 15 уровня", "Набери 1,000 очков", "Набери 10,000 очков", "Набери 50,000 очков", "Набери 100,000 очков", "Уничтожь 2 линии разом", "Уничтожь 3 линии разом", "Уничтожь 4 линии разом", "Сделай 10 Hard Drops", "Поставь 50 фигур", "Поставь 200 фигур", "Очисти поле полностью", "Поверни фигуру 100 раз") else listOf("Clear 1 line", "Clear 10 lines", "Clear 50 lines", "Clear 100 lines", "Reach level 2", "Reach level 5", "Reach level 10", "Reach level 15", "Score 1,000", "Score 10,000", "Score 50,000", "Score 100,000", "Clear 2 lines at once", "Clear 3 lines at once", "Clear 4 lines at once", "Perform 10 Hard Drops", "Place 50 pieces", "Place 200 pieces", "Clear the entire board", "Rotate pieces 100 times")
        return List(20) { i -> Achievement(i + 1, titles[i], descs[i], unlockedIds.contains((i + 1).toString())) }
    }
}