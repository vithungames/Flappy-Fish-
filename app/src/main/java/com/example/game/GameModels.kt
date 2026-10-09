package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class PlayState {
    READY,      // Bouncing in place, waiting for first tap
    PLAYING,    // Active gameplay
    DYING,      // Collision happened, falling to ground with screen flash
    GAME_OVER,  // Scoreboard visible, ready for restart
    PAUSED      // Paused by user
}

data class Bird(
    val x: Float = 0f,
    val y: Float = 0f,
    val velocityY: Float = 0f,
    val rotation: Float = 0f,
    val wingFrame: Int = 0,
    val radius: Float = 20f
)

data class PipePair(
    val id: Long,
    val x: Float,
    val width: Float,
    val gapTopY: Float,
    val gapBottomY: Float,
    val hasScored: Boolean = false,
    val hasCoin: Boolean = false,
    val coinCollected: Boolean = false,
    val coinY: Float = 0f,
    val coinAnimPhase: Float = 0f
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val life: Float,
    val maxLife: Float
)

data class ScorePopup(
    val id: Long,
    val x: Float,
    val y: Float,
    val text: String,
    val color: Color,
    val alpha: Float = 1f,
    val life: Float = 0.8f
)

data class Cloud(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val speed: Float,
    val scale: Float
)
