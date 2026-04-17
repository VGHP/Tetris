# Tetris

An Android application implementing the core mechanics of Tetris, utilizing Jetpack Compose for UI and custom 2D rendering.

## Tech Stack
* **Min SDK:** 29
* **Target/Compile SDK:** 36
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3, Canvas API)
* **Concurrency:** Kotlin Coroutines, StateFlow
* **Persistence:** AndroidX DataStore (Preferences)
* **Architecture:** MVVM

## Core Components & Features
* **State Management:** Unidirectional data flow. Game logic and UI state are encapsulated within `TetrisViewModel`, which exposes a single `StateFlow<GameState>`.
* **Custom Rendering:** The grid, tetrominoes, and visual effects (e.g., drop trails, clear line animations, ghost pieces) are rendered exclusively via the Compose `Canvas` API using calculated pixel offsets.
* **Input Handling:** Implemented via Compose pointer input modifiers:
  * `detectTapGestures`: Triggers matrix rotation.
  * `detectDragGestures`: Calculates horizontal drag distance for X-axis piece translation, and vertical velocity/distance thresholds for instant hard drops.
* **Theming Engine:** 5 rendering configurations (Neon, Nyan Cat, Retro, Classic, Minimal). Themes dynamically swap color matrices (`ThemeColors`) and specific `Canvas` drawing routines (e.g., gradient backgrounds, shadow bevels) at runtime.
* **Audio & Haptics:** `SoundManager` utilizes `MediaPlayer` for looping background music and `SoundPool` for low-latency SFX. `HapticFeedback` is integrated for matrix interactions and state changes.
* **Data Persistence:** `SettingsStore` provides asynchronous read/write operations via `DataStore` to persist the high score, application settings (volume, theme, language), and a `Set<String>` of unlocked achievement IDs.
* **Splash Screen:** A `Canvas`-based progress indicator driven by Compose `animateFloatAsState` and `tween` animation specifications.

## Project Structure
* `ui/`: Compose screen definitions (`TetrisScreen`, `SplashScreen`) and custom `DrawScope` extensions.
* `viewmodel/`: `TetrisViewModel` containing the game loop ticker, collision detection algorithms, and state mutations.
* `model/`: Domain data classes (`GameState`, `Achievement`), enums (`Tetromino`, `AppTheme`), and `ThemeConfig`.
* `data/`: `SettingsStore` (DataStore implementation).
* `audio/`: `SoundManager` (MediaPlayer/SoundPool wrapper).

## Build Instructions
1. Clone the repository.
2. Open the project in Android Studio.
3. Ensure JDK 17 is configured in the Gradle settings.
4. Build and deploy to an Android device or emulator running API 29 or higher.

## License

Distributed under the [MIT License](LICENSE).