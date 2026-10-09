package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.database.FlappyDatabase
import com.example.data.entity.ScoreRecord
import com.example.data.entity.UserProfile
import com.example.data.model.BirdSkin
import com.example.data.model.GameDifficulty
import com.example.data.model.MedalType
import com.example.data.model.WorldTheme
import com.example.data.repository.GameRepository
import com.example.game.GameEngine
import com.example.game.PlayState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameUiState(
    val playState: PlayState = PlayState.READY,
    val score: Int = 0,
    val coinsThisRound: Int = 0,
    val isNewBest: Boolean = false,
    val medalAwarded: MedalType = MedalType.NONE,
    val currentBest: Int = 0,
    val showPauseDialog: Boolean = false,
    val showSkinsDialog: Boolean = false,
    val showStatsDialog: Boolean = false,
    val showDifficultyDialog: Boolean = false,
    val showThemeDialog: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val engine = GameEngine()
    val soundManager = SoundManager(application)
    private val repository: GameRepository

    val profile: StateFlow<UserProfile>
    val topScores: StateFlow<List<ScoreRecord>>

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastFrameTimeNanos: Long = 0L
    private var previousPlayState: PlayState = PlayState.READY

    init {
        val db = FlappyDatabase.getInstance(application)
        repository = GameRepository(db.gameDao())

        profile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserProfile()
        )

        topScores = repository.topScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.getOrCreateProfile()
            profile.collect { p ->
                soundManager.soundEnabled = p.soundEnabled
                soundManager.vibrateEnabled = p.vibrateEnabled
                updateCurrentBestScore(p)
            }
        }

        startGameLoop()
    }

    private fun updateCurrentBestScore(p: UserProfile) {
        val currentDiff = GameDifficulty.fromId(p.selectedDifficulty)
        val best = when (currentDiff) {
            GameDifficulty.CASUAL -> p.bestScoreCasual
            GameDifficulty.HARDCORE -> p.bestScoreHardcore
            else -> p.bestScoreClassic
        }
        _uiState.value = _uiState.value.copy(currentBest = best)
    }

    fun onCanvasSizeChanged(width: Float, height: Float) {
        val currentDiff = GameDifficulty.fromId(profile.value.selectedDifficulty)
        engine.initDimensions(width, height, currentDiff)
        updateCurrentBestScore(profile.value)
    }

    fun onTap() {
        if (_uiState.value.showPauseDialog ||
            _uiState.value.showSkinsDialog ||
            _uiState.value.showStatsDialog ||
            _uiState.value.showDifficultyDialog ||
            _uiState.value.showThemeDialog
        ) {
            return
        }

        val handled = engine.onTap(soundManager)
        if (handled) {
            _uiState.value = _uiState.value.copy(playState = engine.playState)
        }
    }

    fun onPauseClicked() {
        if (engine.playState == PlayState.PLAYING) {
            engine.pause()
            _uiState.value = _uiState.value.copy(
                playState = PlayState.PAUSED,
                showPauseDialog = true
            )
        }
    }

    fun onResumeClicked() {
        engine.resume()
        _uiState.value = _uiState.value.copy(
            playState = PlayState.PLAYING,
            showPauseDialog = false
        )
    }

    fun onRestartClicked() {
        engine.resetToReady()
        _uiState.value = _uiState.value.copy(
            playState = PlayState.READY,
            score = 0,
            coinsThisRound = 0,
            isNewBest = false,
            medalAwarded = MedalType.NONE,
            showPauseDialog = false
        )
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            lastFrameTimeNanos = 0L
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos == 0L) {
                        lastFrameTimeNanos = frameTimeNanos
                        return@withFrameNanos
                    }

                    val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                    lastFrameTimeNanos = frameTimeNanos

                    engine.update(dt, soundManager)

                    // Track transitions to GAME_OVER to persist score
                    if (previousPlayState != PlayState.GAME_OVER && engine.playState == PlayState.GAME_OVER) {
                        handleGameOver()
                    }
                    previousPlayState = engine.playState

                    _uiState.value = _uiState.value.copy(
                        playState = engine.playState,
                        score = engine.currentScore,
                        coinsThisRound = engine.coinsEarnedThisRound
                    )
                }
            }
        }
    }

    private fun handleGameOver() {
        val finalScore = engine.currentScore
        val coins = engine.coinsEarnedThisRound
        val flaps = engine.flapsThisRound
        val pipes = engine.pipesPassedThisRound
        val diff = profile.value.selectedDifficulty
        val skinId = profile.value.selectedSkinId
        val medal = MedalType.fromScore(finalScore)

        if (medal != MedalType.NONE) {
            soundManager.playMedal()
        }

        viewModelScope.launch {
            val (updatedProfile, isNewBest) = repository.recordGameFinished(
                score = finalScore,
                difficulty = diff,
                birdSkinId = skinId,
                coinsEarned = coins,
                flapsCount = flaps,
                pipesPassedCount = pipes
            )

            val currentDiff = GameDifficulty.fromId(diff)
            val best = when (currentDiff) {
                GameDifficulty.CASUAL -> updatedProfile.bestScoreCasual
                GameDifficulty.HARDCORE -> updatedProfile.bestScoreHardcore
                else -> updatedProfile.bestScoreClassic
            }

            _uiState.value = _uiState.value.copy(
                isNewBest = isNewBest,
                medalAwarded = medal,
                currentBest = best
            )
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.soundEnabled = enabled
        viewModelScope.launch {
            repository.updateSettings(
                sound = enabled,
                vibrate = profile.value.vibrateEnabled,
                difficulty = profile.value.selectedDifficulty,
                theme = profile.value.selectedTheme
            )
        }
    }

    fun setVibrateEnabled(enabled: Boolean) {
        soundManager.vibrateEnabled = enabled
        viewModelScope.launch {
            repository.updateSettings(
                sound = profile.value.soundEnabled,
                vibrate = enabled,
                difficulty = profile.value.selectedDifficulty,
                theme = profile.value.selectedTheme
            )
        }
    }

    fun selectDifficulty(diff: GameDifficulty) {
        viewModelScope.launch {
            repository.updateSettings(
                sound = profile.value.soundEnabled,
                vibrate = profile.value.vibrateEnabled,
                difficulty = diff.id,
                theme = profile.value.selectedTheme
            )
            engine.initDimensions(engine.screenWidth, engine.screenHeight, diff)
            updateCurrentBestScore(profile.value.copy(selectedDifficulty = diff.id))
            onRestartClicked()
        }
        _uiState.value = _uiState.value.copy(showDifficultyDialog = false)
    }

    fun selectTheme(theme: WorldTheme) {
        viewModelScope.launch {
            repository.updateSettings(
                sound = profile.value.soundEnabled,
                vibrate = profile.value.vibrateEnabled,
                difficulty = profile.value.selectedDifficulty,
                theme = theme.id
            )
        }
        _uiState.value = _uiState.value.copy(showThemeDialog = false)
    }

    fun selectSkin(skin: BirdSkin) {
        viewModelScope.launch {
            repository.selectSkin(skin.id)
        }
    }

    fun unlockSkin(skin: BirdSkin, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockSkin(skin.id, skin.unlockPrice)
            if (success) {
                soundManager.playCoin()
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun openSkinsDialog() {
        _uiState.value = _uiState.value.copy(showSkinsDialog = true)
    }

    fun closeSkinsDialog() {
        _uiState.value = _uiState.value.copy(showSkinsDialog = false)
    }

    fun openStatsDialog() {
        _uiState.value = _uiState.value.copy(showStatsDialog = true)
    }

    fun closeStatsDialog() {
        _uiState.value = _uiState.value.copy(showStatsDialog = false)
    }

    fun openDifficultyDialog() {
        _uiState.value = _uiState.value.copy(showDifficultyDialog = true)
    }

    fun closeDifficultyDialog() {
        _uiState.value = _uiState.value.copy(showDifficultyDialog = false)
    }

    fun openThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = true)
    }

    fun closeThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = false)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        soundManager.release()
    }
}
