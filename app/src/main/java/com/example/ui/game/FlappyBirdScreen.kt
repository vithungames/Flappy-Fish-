package com.example.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BirdSkin
import com.example.data.model.GameDifficulty
import com.example.data.model.WorldTheme
import com.example.game.PlayState
import com.example.ui.dialogs.DifficultyDialog
import com.example.ui.dialogs.GameOverDialog
import com.example.ui.dialogs.PauseDialog
import com.example.ui.dialogs.SkinsDialog
import com.example.ui.dialogs.StatsDialog
import com.example.ui.dialogs.ThemeDialog
import com.example.viewmodel.GameViewModel

@Composable
fun FlappyBirdScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val topScores by viewModel.topScores.collectAsStateWithLifecycle()

    val currentTheme = remember(profile.selectedTheme) {
        WorldTheme.fromId(profile.selectedTheme)
    }
    val currentSkin = remember(profile.selectedSkinId) {
        BirdSkin.getById(profile.selectedSkinId)
    }
    val currentDifficulty = remember(profile.selectedDifficulty) {
        GameDifficulty.fromId(profile.selectedDifficulty)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("flappy_bird_screen")
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(widthPx, heightPx, profile.selectedDifficulty) {
            viewModel.onCanvasSizeChanged(widthPx, heightPx)
        }

        // Tap-to-flap detector over the full screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            viewModel.onTap()
                        }
                    )
                }
        ) {
            // Main 60FPS Game Canvas
            Canvas(modifier = Modifier.fillMaxSize().testTag("game_canvas")) {
                GameRenderer.drawGame(
                    drawScope = this,
                    engine = viewModel.engine,
                    theme = currentTheme,
                    skin = currentSkin
                )
            }
        }

        // HUD Overlay respecting window insets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            GameHud(
                playState = uiState.playState,
                score = uiState.score,
                coins = profile.coins + uiState.coinsThisRound,
                difficultyName = currentDifficulty.title,
                onPauseClick = { viewModel.onPauseClicked() }
            )

            // Game Over Scoreboard Dialog
            AnimatedVisibility(
                visible = uiState.playState == PlayState.GAME_OVER,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameOverDialog(
                    score = uiState.score,
                    bestScore = uiState.currentBest,
                    isNewBest = uiState.isNewBest,
                    coinsEarned = uiState.coinsThisRound,
                    medal = uiState.medalAwarded,
                    onRestart = { viewModel.onRestartClicked() },
                    onOpenSkins = { viewModel.openSkinsDialog() },
                    onOpenStats = { viewModel.openStatsDialog() },
                    onOpenThemes = { viewModel.openThemeDialog() },
                    onOpenDifficulty = { viewModel.openDifficultyDialog() }
                )
            }
        }

        // Modals & Dialogs
        if (uiState.showPauseDialog) {
            PauseDialog(
                soundEnabled = profile.soundEnabled,
                vibrateEnabled = profile.vibrateEnabled,
                onResume = { viewModel.onResumeClicked() },
                onRestart = { viewModel.onRestartClicked() },
                onToggleSound = { viewModel.setSoundEnabled(it) },
                onToggleVibrate = { viewModel.setVibrateEnabled(it) },
                onDismiss = { viewModel.onResumeClicked() }
            )
        }

        if (uiState.showSkinsDialog) {
            SkinsDialog(
                userCoins = profile.coins,
                unlockedSkinsCsv = profile.unlockedSkinsCsv,
                selectedSkinId = profile.selectedSkinId,
                onSelectSkin = { viewModel.selectSkin(it) },
                onUnlockSkin = { skin, onSuccess, onError ->
                    viewModel.unlockSkin(skin, onSuccess, onError)
                },
                onDismiss = { viewModel.closeSkinsDialog() }
            )
        }

        if (uiState.showStatsDialog) {
            StatsDialog(
                profile = profile,
                topScores = topScores,
                onDismiss = { viewModel.closeStatsDialog() }
            )
        }

        if (uiState.showDifficultyDialog) {
            DifficultyDialog(
                currentDifficultyId = profile.selectedDifficulty,
                onSelectDifficulty = { viewModel.selectDifficulty(it) },
                onDismiss = { viewModel.closeDifficultyDialog() }
            )
        }

        if (uiState.showThemeDialog) {
            ThemeDialog(
                currentThemeId = profile.selectedTheme,
                onSelectTheme = { viewModel.selectTheme(it) },
                onDismiss = { viewModel.closeThemeDialog() }
            )
        }
    }
}
