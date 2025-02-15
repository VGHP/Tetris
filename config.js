const GAME_CONFIG = {
    // Размеры игрового поля
    BOARD_WIDTH: 14,
    BOARD_HEIGHT: 20,
    BLOCK_SIZE: 32,

    // Настройки уровней сложности
    INITIAL_LEVEL: 0,
    MAX_LEVEL: 20,
    POINTS_PER_LEVEL: 1000,    // Очки для перехода на следующий уровень
    SPEED_COEFFICIENT: 0.8,     // Коэффициент ускорения при переходе на новый уровень
    INITIAL_SPEED: 1000,        // Начальная скорость падения (в миллисекундах)

    // Очки за действия
    POINTS: {
        SINGLE_LINE: 100,       // За одну линию
        DOUBLE_LINE: 300,       // За две линии
        TRIPLE_LINE: 500,       // За три линии
        TETRIS: 800,           // За четыре линии (тетрис)
        SOFT_DROP: 1,          // За ускорение падения (за каждую клетку)
        HARD_DROP: 2           // За мгновенное падение (за каждую клетку)
    }
}; 