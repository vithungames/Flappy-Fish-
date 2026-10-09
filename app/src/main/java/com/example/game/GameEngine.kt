package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundManager
import com.example.data.model.GameDifficulty
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine {

    var playState: PlayState = PlayState.READY
        private set

    var bird: Bird = Bird()
        private set

    var pipes: List<PipePair> = emptyList()
        private set

    var particles: List<Particle> = emptyList()
        private set

    var scorePopups: List<ScorePopup> = emptyList()
        private set

    var clouds: List<Cloud> = emptyList()
        private set

    var currentScore: Int = 0
        private set

    var coinsEarnedThisRound: Int = 0
        private set

    var flapsThisRound: Long = 0L
        private set

    var pipesPassedThisRound: Long = 0L
        private set

    var groundOffset: Float = 0f
        private set

    var screenShake: Offset = Offset.Zero
        private set

    var flashAlpha: Float = 0f
        private set

    var screenWidth: Float = 1080f
        private set

    var screenHeight: Float = 1920f
        private set

    var groundY: Float = 1680f
        private set

    // Configuration derived from difficulty
    private var currentDifficulty: GameDifficulty = GameDifficulty.CLASSIC
    private var pipeSpeed: Float = 250f
    private var pipeGapHeight: Float = 210f
    private var pipeSpacing: Float = 360f
    private var gravity: Float = 1750f
    private var flapStrength: Float = -560f
    private var pipeWidth: Float = 110f

    // Internal state timers
    private var readyHoverTimer: Float = 0f
    private var wingAnimTimer: Float = 0f
    private var shakeTimer: Float = 0f
    private var lastPipeId: Long = 0L
    private var nextPopupId: Long = 0L

    fun initDimensions(width: Float, height: Float, difficulty: GameDifficulty) {
        if (width <= 0f || height <= 0f) return
        screenWidth = width
        screenHeight = height
        val groundHeight = (height * 0.16f).coerceIn(120f, 260f)
        groundY = height - groundHeight
        currentDifficulty = difficulty

        val scale = height / 1000f
        pipeSpeed = difficulty.baseSpeed * scale
        pipeGapHeight = difficulty.gapHeightDp * (scale * 0.95f)
        pipeSpacing = screenWidth * difficulty.spawnDistanceRatio + 120f
        gravity = 1700f * difficulty.gravityFactor * scale
        flapStrength = -550f * scale
        pipeWidth = (80f * scale).coerceIn(75f, 130f)

        if (clouds.isEmpty()) {
            initClouds()
        }

        resetToReady()
    }

    private fun initClouds() {
        val list = mutableListOf<Cloud>()
        for (i in 0 until 5) {
            val scale = Random.nextFloat() * 0.6f + 0.7f
            list.add(
                Cloud(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * (groundY * 0.45f) + 40f,
                    width = 160f * scale,
                    height = 60f * scale,
                    speed = (20f + Random.nextFloat() * 25f),
                    scale = scale
                )
            )
        }
        clouds = list
    }

    fun resetToReady() {
        playState = PlayState.READY
        currentScore = 0
        coinsEarnedThisRound = 0
        flapsThisRound = 0L
        pipesPassedThisRound = 0L
        readyHoverTimer = 0f
        wingAnimTimer = 0f
        shakeTimer = 0f
        screenShake = Offset.Zero
        flashAlpha = 0f
        pipes = emptyList()
        particles = emptyList()
        scorePopups = emptyList()

        val birdX = screenWidth * 0.28f
        val birdY = (groundY) * 0.46f
        bird = Bird(
            x = birdX,
            y = birdY,
            velocityY = 0f,
            rotation = 0f,
            wingFrame = 0,
            radius = (screenWidth * 0.045f).coerceIn(18f, 28f)
        )
    }

    fun onTap(soundManager: SoundManager): Boolean {
        when (playState) {
            PlayState.READY -> {
                playState = PlayState.PLAYING
                doFlap(soundManager)
                spawnInitialPipes()
                return true
            }
            PlayState.PLAYING -> {
                doFlap(soundManager)
                return true
            }
            PlayState.PAUSED -> {
                resume()
                return true
            }
            PlayState.DYING, PlayState.GAME_OVER -> {
                return false
            }
        }
    }

    private fun doFlap(soundManager: SoundManager) {
        flapsThisRound++
        bird = bird.copy(
            velocityY = flapStrength,
            rotation = -26f,
            wingFrame = 2
        )
        soundManager.playFlap()

        // Spawn gentle feather particles behind bird
        val newParticles = particles.toMutableList()
        for (i in 0..2) {
            val angle = Random.nextFloat() * 0.8f + 2.7f // angled backwards
            val speed = Random.nextFloat() * 120f + 60f
            newParticles.add(
                Particle(
                    x = bird.x - bird.radius * 0.7f,
                    y = bird.y + (Random.nextFloat() - 0.5f) * bird.radius,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = Color.White.copy(alpha = 0.8f),
                    size = Random.nextFloat() * 6f + 4f,
                    life = 0f,
                    maxLife = 0.35f
                )
            )
        }
        particles = newParticles
    }

    private fun spawnInitialPipes() {
        val list = mutableListOf<PipePair>()
        val startX = screenWidth + 120f
        for (i in 0 until 3) {
            val pipeX = startX + i * pipeSpacing
            list.add(createPipePair(pipeX))
        }
        pipes = list
    }

    private fun createPipePair(x: Float): PipePair {
        lastPipeId++
        val minGapTop = 130f
        val maxGapTop = groundY - pipeGapHeight - 110f
        val gapTop = Random.nextFloat() * (maxGapTop - minGapTop) + minGapTop
        val gapBottom = gapTop + pipeGapHeight

        val hasCoin = Random.nextFloat() < 0.42f
        val coinY = gapTop + pipeGapHeight * 0.5f

        return PipePair(
            id = lastPipeId,
            x = x,
            width = pipeWidth,
            gapTopY = gapTop,
            gapBottomY = gapBottom,
            hasScored = false,
            hasCoin = hasCoin,
            coinCollected = false,
            coinY = coinY,
            coinAnimPhase = Random.nextFloat() * 6.28f
        )
    }

    fun pause() {
        if (playState == PlayState.PLAYING) {
            playState = PlayState.PAUSED
        }
    }

    fun resume() {
        if (playState == PlayState.PAUSED) {
            playState = PlayState.PLAYING
        }
    }

    fun update(dtSeconds: Float, soundManager: SoundManager) {
        val dt = dtSeconds.coerceIn(0f, 0.05f)

        // Screen shake decay
        if (shakeTimer > 0f) {
            shakeTimer -= dt
            val intensity = (shakeTimer / 0.22f) * 16f
            screenShake = Offset(
                (Random.nextFloat() - 0.5f) * intensity,
                (Random.nextFloat() - 0.5f) * intensity
            )
        } else {
            screenShake = Offset.Zero
        }

        // Screen flash decay
        if (flashAlpha > 0f) {
            flashAlpha = (flashAlpha - dt * 3.5f).coerceAtLeast(0f)
        }

        // Update clouds parallax
        clouds = clouds.map { cloud ->
            var newX = cloud.x - cloud.speed * dt
            if (newX + cloud.width < 0) {
                newX = screenWidth + Random.nextFloat() * 80f
            }
            cloud.copy(x = newX)
        }

        // Update particle life
        if (particles.isNotEmpty()) {
            particles = particles.mapNotNull { p ->
                val newLife = p.life + dt
                if (newLife >= p.maxLife) null
                else p.copy(
                    x = p.x + p.vx * dt,
                    y = p.y + p.vy * dt + 180f * dt, // slight gravity on particles
                    life = newLife
                )
            }
        }

        // Update score popups
        if (scorePopups.isNotEmpty()) {
            scorePopups = scorePopups.mapNotNull { popup ->
                val newLife = popup.life - dt
                if (newLife <= 0f) null
                else popup.copy(
                    y = popup.y - 65f * dt,
                    alpha = (newLife / 0.8f).coerceIn(0f, 1f),
                    life = newLife
                )
            }
        }

        when (playState) {
            PlayState.READY -> updateReadyState(dt)
            PlayState.PLAYING -> updatePlayingState(dt, soundManager)
            PlayState.DYING -> updateDyingState(dt, soundManager)
            PlayState.PAUSED, PlayState.GAME_OVER -> {
                // Static
            }
        }
    }

    private fun updateReadyState(dt: Float) {
        readyHoverTimer += dt * 4.5f
        val hoverY = (groundY * 0.46f) + sin(readyHoverTimer) * 14f

        wingAnimTimer += dt * 10f
        val wingFrame = (wingAnimTimer.toInt()) % 3

        bird = bird.copy(
            y = hoverY,
            velocityY = 0f,
            rotation = 0f,
            wingFrame = wingFrame
        )

        // Scroll ground gently in ready mode for alive feeling
        groundOffset = (groundOffset + pipeSpeed * 0.6f * dt) % 48f
    }

    private fun updatePlayingState(dt: Float, soundManager: SoundManager) {
        // Scroll ground
        groundOffset = (groundOffset + pipeSpeed * dt) % 48f

        // 1. Physics update for Bird
        val newVelocity = (bird.velocityY + gravity * dt).coerceAtMost(980f)
        val newY = bird.y + newVelocity * dt

        // Rotation calculation: fast tilt up on flap, smooth dive down to +85° as velocity increases
        val targetRotation = when {
            newVelocity < 0 -> -24f
            newVelocity < 200f -> (-24f + (newVelocity / 200f) * 24f)
            else -> {
                val diveProgress = ((newVelocity - 200f) / 500f).coerceIn(0f, 1f)
                diveProgress * 85f
            }
        }

        val smoothRotation = bird.rotation + (targetRotation - bird.rotation) * (dt * 12f).coerceAtMost(1f)

        // Wing flap animation
        wingAnimTimer += dt * if (newVelocity < 100f) 16f else 6f
        val wingFrame = if (smoothRotation > 40f) 0 else (wingAnimTimer.toInt()) % 3

        bird = bird.copy(
            y = newY,
            velocityY = newVelocity,
            rotation = smoothRotation,
            wingFrame = wingFrame
        )

        // Ceiling collision
        if (bird.y - bird.radius <= 0f) {
            bird = bird.copy(y = bird.radius, velocityY = 0f)
        }

        // Ground collision
        if (bird.y + bird.radius >= groundY) {
            triggerDeath(collidedWithGround = true, soundManager = soundManager)
            return
        }

        // 2. Pipes update and collision check
        val currentPipes = pipes.toMutableList()
        var birdCollided = false

        for (i in currentPipes.indices) {
            val pipe = currentPipes[i]
            val newX = pipe.x - pipeSpeed * dt

            // Score check (bird passed pipe center)
            var hasScored = pipe.hasScored
            if (!hasScored && (bird.x > newX + pipe.width * 0.5f)) {
                hasScored = true
                currentScore++
                pipesPassedThisRound++
                soundManager.playPoint()
                addScorePopup(bird.x, bird.y - 30f, "+1", Color(0xFFFFEB3B))
            }

            // Coin check
            var coinCollected = pipe.coinCollected
            if (pipe.hasCoin && !coinCollected) {
                val coinDistSq = distSq(bird.x, bird.y, newX + pipe.width * 0.5f, pipe.coinY)
                val coinHitRadius = bird.radius + 18f
                if (coinDistSq <= coinHitRadius * coinHitRadius) {
                    coinCollected = true
                    coinsEarnedThisRound++
                    soundManager.playCoin()
                    spawnCoinParticles(newX + pipe.width * 0.5f, pipe.coinY)
                    addScorePopup(newX + pipe.width * 0.5f, pipe.coinY - 20f, "+COIN", Color(0xFFFFD700))
                }
            }

            currentPipes[i] = pipe.copy(
                x = newX,
                hasScored = hasScored,
                coinCollected = coinCollected,
                coinAnimPhase = pipe.coinAnimPhase + dt * 4f
            )

            // Collision check with Top Pipe and Bottom Pipe
            if (checkPipeCollision(bird, currentPipes[i])) {
                birdCollided = true
            }
        }

        // Remove offscreen pipes and spawn new ones
        if (currentPipes.isNotEmpty() && (currentPipes[0].x + currentPipes[0].width < -20f)) {
            currentPipes.removeAt(0)
            val lastPipeX = currentPipes.lastOrNull()?.x ?: screenWidth
            currentPipes.add(createPipePair(lastPipeX + pipeSpacing))
        }

        pipes = currentPipes

        if (birdCollided) {
            triggerDeath(collidedWithGround = false, soundManager = soundManager)
        }
    }

    private fun updateDyingState(dt: Float, soundManager: SoundManager) {
        // Bird dives fast to the ground, ground doesn't move
        val newVelocity = (bird.velocityY + gravity * 1.25f * dt).coerceAtMost(1200f)
        val newY = bird.y + newVelocity * dt
        val newRotation = (bird.rotation + dt * 400f).coerceAtMost(90f)

        if (newY + bird.radius >= groundY) {
            // Reached ground, finish death sequence
            bird = bird.copy(
                y = groundY - bird.radius,
                velocityY = 0f,
                rotation = 90f
            )
            playState = PlayState.GAME_OVER
        } else {
            bird = bird.copy(
                y = newY,
                velocityY = newVelocity,
                rotation = newRotation
            )
        }
    }

    private fun triggerDeath(collidedWithGround: Boolean, soundManager: SoundManager) {
        soundManager.playHit()
        if (!collidedWithGround) {
            soundManager.playDie()
        }

        flashAlpha = 0.85f
        shakeTimer = 0.22f

        // Spawn hit crash sparks
        val newParticles = particles.toMutableList()
        for (i in 0 until 14) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 260f + 60f
            val colors = listOf(Color.White, Color(0xFFFFD54F), Color(0xFFFF7043), Color(0xFFE53935))
            newParticles.add(
                Particle(
                    x = bird.x,
                    y = bird.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = colors.random(),
                    size = Random.nextFloat() * 7f + 4f,
                    life = 0f,
                    maxLife = 0.45f
                )
            )
        }
        particles = newParticles

        if (collidedWithGround) {
            bird = bird.copy(
                y = groundY - bird.radius,
                velocityY = 0f,
                rotation = 90f
            )
            playState = PlayState.GAME_OVER
        } else {
            // Start falling to ground
            bird = bird.copy(
                velocityY = -180f, // slight hop on hit
                rotation = 35f
            )
            playState = PlayState.DYING
        }
    }

    private fun checkPipeCollision(bird: Bird, pipe: PipePair): Boolean {
        // Slightly inset bird collision circle to make it fair and feel great
        val hitRadius = bird.radius * 0.82f
        val bx = bird.x
        val by = bird.y

        // Pipe rim overhang
        val rimExtra = 8f
        val pipeLeft = pipe.x - rimExtra
        val pipeRight = pipe.x + pipe.width + rimExtra

        // 1. Top pipe: extends from 0 down to gapTopY
        val topPipeBottom = pipe.gapTopY
        if (circleIntersectsAabb(bx, by, hitRadius, pipeLeft, 0f, pipeRight, topPipeBottom)) {
            return true
        }

        // 2. Bottom pipe: extends from gapBottomY down to groundY
        val bottomPipeTop = pipe.gapBottomY
        if (circleIntersectsAabb(bx, by, hitRadius, pipeLeft, bottomPipeTop, pipeRight, groundY)) {
            return true
        }

        return false
    }

    private fun circleIntersectsAabb(
        cx: Float, cy: Float, radius: Float,
        left: Float, top: Float, right: Float, bottom: Float
    ): Boolean {
        val closestX = cx.coerceIn(left, right)
        val closestY = cy.coerceIn(top, bottom)
        val dx = cx - closestX
        val dy = cy - closestY
        return (dx * dx + dy * dy) < (radius * radius)
    }

    private fun spawnCoinParticles(x: Float, y: Float) {
        val newParticles = particles.toMutableList()
        for (i in 0 until 10) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 180f + 50f
            newParticles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 60f,
                    color = Color(0xFFFFD700),
                    size = Random.nextFloat() * 6f + 3f,
                    life = 0f,
                    maxLife = 0.5f
                )
            )
        }
        particles = newParticles
    }

    private fun addScorePopup(x: Float, y: Float, text: String, color: Color) {
        nextPopupId++
        scorePopups = scorePopups + ScorePopup(
            id = nextPopupId,
            x = x,
            y = y,
            text = text,
            color = color
        )
    }

    private fun distSq(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }
}
