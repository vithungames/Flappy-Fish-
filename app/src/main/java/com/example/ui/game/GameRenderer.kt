package com.example.ui.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.example.data.model.BirdSkin
import com.example.data.model.WorldTheme
import com.example.game.Bird
import com.example.game.Cloud
import com.example.game.GameEngine
import com.example.game.Particle
import com.example.game.PipePair
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {

    fun drawGame(
        drawScope: DrawScope,
        engine: GameEngine,
        theme: WorldTheme,
        skin: BirdSkin
    ) {
        with(drawScope) {
            // Apply screen shake
            translate(engine.screenShake.x, engine.screenShake.y) {
                // 1. Sky & Horizon
                drawSkyAndBackdrop(engine, theme)

                // 2. Parallax Clouds
                drawClouds(engine.clouds, theme)

                // 3. City & Bush Horizon Silhouette
                drawHorizonCity(engine, theme)

                // 4. Pipes
                for (pipe in engine.pipes) {
                    drawPipePair(pipe, engine.groundY, theme)
                }

                // 5. Ground
                drawGround(engine, theme)

                // 6. Particles
                drawParticles(engine.particles)

                // 7. Bird
                drawBird(engine.bird, skin)
            }

            // 8. White Hit Flash overlay (unaffected by shake)
            if (engine.flashAlpha > 0f) {
                drawRect(
                    color = Color.White.copy(alpha = engine.flashAlpha),
                    size = size
                )
            }
        }
    }

    private fun DrawScope.drawSkyAndBackdrop(engine: GameEngine, theme: WorldTheme) {
        // Gradient sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.skyTopColor, theme.skyBottomColor),
                startY = 0f,
                endY = engine.groundY
            ),
            size = Size(size.width, engine.groundY)
        )

        // Celestial Body: Moon or Sun
        if (theme.isNight) {
            // Night stars
            val starPositions = listOf(
                Offset(0.15f * size.width, 0.12f * engine.groundY),
                Offset(0.35f * size.width, 0.08f * engine.groundY),
                Offset(0.62f * size.width, 0.15f * engine.groundY),
                Offset(0.78f * size.width, 0.06f * engine.groundY),
                Offset(0.88f * size.width, 0.22f * engine.groundY),
                Offset(0.24f * size.width, 0.28f * engine.groundY),
                Offset(0.52f * size.width, 0.32f * engine.groundY),
                Offset(0.70f * size.width, 0.38f * engine.groundY)
            )
            for (pos in starPositions) {
                drawCircle(Color.White.copy(alpha = 0.85f), radius = 2.5f, center = pos)
            }

            // Golden Crescent Moon
            val moonCenter = Offset(size.width * 0.82f, engine.groundY * 0.15f)
            drawCircle(Color(0xFFFFEE88), radius = 28f, center = moonCenter)
            drawCircle(
                theme.skyTopColor,
                radius = 24f,
                center = Offset(moonCenter.x - 10f, moonCenter.y - 6f)
            )
        } else {
            // Bright Warm Sun
            val sunCenter = Offset(size.width * 0.82f, engine.groundY * 0.14f)
            // Sun aura
            drawCircle(
                Color.White.copy(alpha = 0.25f),
                radius = 42f,
                center = sunCenter
            )
            drawCircle(
                Color(0xFFFFF9C4),
                radius = 28f,
                center = sunCenter
            )
        }
    }

    private fun DrawScope.drawClouds(clouds: List<Cloud>, theme: WorldTheme) {
        val cloudColor = if (theme.isNight) Color(0x334A6572) else Color(0xCCFFFFFF)
        val cloudShade = if (theme.isNight) Color(0x2237474F) else Color(0x99ECEFF1)

        for (c in clouds) {
            val cx = c.x
            val cy = c.y
            val w = c.width
            val h = c.height

            // Cloud base pill
            drawRoundRect(
                color = cloudShade,
                topLeft = Offset(cx, cy + h * 0.35f),
                size = Size(w, h * 0.65f),
                cornerRadius = CornerRadius(h * 0.35f, h * 0.35f)
            )
            drawRoundRect(
                color = cloudColor,
                topLeft = Offset(cx, cy + h * 0.3f),
                size = Size(w, h * 0.6f),
                cornerRadius = CornerRadius(h * 0.35f, h * 0.35f)
            )

            // Puffs
            drawCircle(cloudColor, radius = h * 0.42f, center = Offset(cx + w * 0.32f, cy + h * 0.38f))
            drawCircle(cloudColor, radius = h * 0.54f, center = Offset(cx + w * 0.56f, cy + h * 0.28f))
            drawCircle(cloudColor, radius = h * 0.36f, center = Offset(cx + w * 0.78f, cy + h * 0.42f))
        }
    }

    private fun DrawScope.drawHorizonCity(engine: GameEngine, theme: WorldTheme) {
        val horizonY = engine.groundY
        val baseBuildingColor = if (theme.isNight) Color(0xFF132238) else Color(0xFF78C0A8).copy(alpha = 0.55f)
        val bushColor = if (theme.isNight) Color(0xFF193148) else Color(0xFF5EAA80).copy(alpha = 0.75f)

        // Distant buildings
        val buildingWidth = size.width / 12f
        for (i in 0..12) {
            val h = ((i * 37) % 70) + 40f
            val bx = i * buildingWidth
            val by = horizonY - h
            drawRect(
                color = baseBuildingColor,
                topLeft = Offset(bx, by),
                size = Size(buildingWidth + 2f, h)
            )
            // Little lit window dots for night
            if (theme.isNight) {
                val winColor = if (i % 2 == 0) Color(0xFFFFD54F).copy(alpha = 0.7f) else Color(0x44FFFFFF)
                drawRect(
                    color = winColor,
                    topLeft = Offset(bx + buildingWidth * 0.3f, by + 12f),
                    size = Size(4f, 4f)
                )
                drawRect(
                    color = winColor,
                    topLeft = Offset(bx + buildingWidth * 0.6f, by + 26f),
                    size = Size(4f, 4f)
                )
            }
        }

        // Foreground rolling bushes
        val bushStep = 55f
        var x = 0f
        while (x < size.width + bushStep) {
            val radius = 32f
            drawCircle(
                color = bushColor,
                radius = radius,
                center = Offset(x, horizonY - 4f)
            )
            x += bushStep
        }
    }

    private fun DrawScope.drawPipePair(pipe: PipePair, groundY: Float, theme: WorldTheme) {
        val pipeLeft = pipe.x
        val pipeWidth = pipe.width
        val rimOverhang = 7f
        val rimHeight = 32f
        val borderStroke = 2.5f

        val borderColor = Color(0xFF20350B)

        // === 1. TOP PIPE (extends down from 0 to gapTopY) ===
        val topBodyBottom = pipe.gapTopY - rimHeight
        if (topBodyBottom > 0) {
            // Main vertical pipe body
            drawPipeCylinder(
                x = pipeLeft,
                y = 0f,
                width = pipeWidth,
                height = topBodyBottom,
                theme = theme,
                borderColor = borderColor,
                borderStroke = borderStroke
            )
        }

        // Top pipe rim collar (at bottom of top pipe)
        drawPipeRim(
            x = pipeLeft - rimOverhang,
            y = pipe.gapTopY - rimHeight,
            width = pipeWidth + rimOverhang * 2f,
            height = rimHeight,
            theme = theme,
            borderColor = borderColor,
            borderStroke = borderStroke
        )

        // === 2. BOTTOM PIPE (extends up from groundY to gapBottomY) ===
        val bottomRimTop = pipe.gapBottomY
        // Bottom pipe rim collar (at top of bottom pipe)
        drawPipeRim(
            x = pipeLeft - rimOverhang,
            y = bottomRimTop,
            width = pipeWidth + rimOverhang * 2f,
            height = rimHeight,
            theme = theme,
            borderColor = borderColor,
            borderStroke = borderStroke
        )

        // Bottom pipe vertical body
        val bottomBodyTop = bottomRimTop + rimHeight
        if (groundY > bottomBodyTop) {
            drawPipeCylinder(
                x = pipeLeft,
                y = bottomBodyTop,
                width = pipeWidth,
                height = groundY - bottomBodyTop,
                theme = theme,
                borderColor = borderColor,
                borderStroke = borderStroke
            )
        }

        // === 3. BONUS COIN (if available and uncollected) ===
        if (pipe.hasCoin && !pipe.coinCollected) {
            drawBonusCoin(
                centerX = pipeLeft + pipeWidth * 0.5f,
                centerY = pipe.coinY,
                phase = pipe.coinAnimPhase
            )
        }
    }

    private fun DrawScope.drawPipeCylinder(
        x: Float, y: Float, width: Float, height: Float,
        theme: WorldTheme, borderColor: Color, borderStroke: Float
    ) {
        // Base fill with horizontal 3D cylindrical lighting
        val gradient = Brush.horizontalGradient(
            0.0f to theme.pipeBodyDark,
            0.20f to theme.pipeRimLight,
            0.45f to theme.pipeBodyLight,
            0.85f to theme.pipeBodyDark,
            1.0f to borderColor,
            startX = x,
            endX = x + width
        )
        drawRect(
            brush = gradient,
            topLeft = Offset(x, y),
            size = Size(width, height)
        )
        // Dark outline border
        drawRect(
            color = borderColor,
            topLeft = Offset(x, y),
            size = Size(width, height),
            style = Stroke(borderStroke)
        )
    }

    private fun DrawScope.drawPipeRim(
        x: Float, y: Float, width: Float, height: Float,
        theme: WorldTheme, borderColor: Color, borderStroke: Float
    ) {
        val rimGradient = Brush.horizontalGradient(
            0.0f to theme.pipeRimDark,
            0.18f to theme.pipeRimLight,
            0.50f to theme.pipeBodyLight,
            0.88f to theme.pipeRimDark,
            1.0f to borderColor,
            startX = x,
            endX = x + width
        )
        // Rim body
        drawRoundRect(
            brush = rimGradient,
            topLeft = Offset(x, y),
            size = Size(width, height),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Rim border
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(x, y),
            size = Size(width, height),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(borderStroke)
        )
    }

    private fun DrawScope.drawBonusCoin(centerX: Float, centerY: Float, phase: Float) {
        // Subtle vertical hovering bob
        val bobY = centerY + sin(phase) * 6f
        // 3D spinning squish factor
        val spinFactor = cos(phase).coerceIn(-1f, 1f)
        val coinRadius = 15f
        val currentWidth = (coinRadius * 2f * spinFactor).let { if (it == 0f) 0.5f else it }
        val absWidth = kotlin.math.abs(currentWidth)

        if (absWidth > 2f) {
            // Golden outer coin
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFF8F00)),
                    center = Offset(centerX, bobY),
                    radius = coinRadius
                ),
                topLeft = Offset(centerX - absWidth * 0.5f, bobY - coinRadius),
                size = Size(absWidth, coinRadius * 2f)
            )
            // Coin edge outline
            drawOval(
                color = Color(0xFFBF360C),
                topLeft = Offset(centerX - absWidth * 0.5f, bobY - coinRadius),
                size = Size(absWidth, coinRadius * 2f),
                style = Stroke(2f)
            )
            // Inner star / coin gleam
            if (absWidth > 8f) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 2.5f,
                    center = Offset(centerX - absWidth * 0.2f, bobY - coinRadius * 0.35f)
                )
            }
        }
    }

    private fun DrawScope.drawGround(engine: GameEngine, theme: WorldTheme) {
        val groundY = engine.groundY
        val groundHeight = size.height - groundY
        val borderColor = Color(0xFF3B2E1E)

        // 1. Earth Soil Bed
        drawRect(
            color = theme.groundSoilColor,
            topLeft = Offset(0f, groundY),
            size = Size(size.width, groundHeight)
        )

        // Soil texture speckles
        val speckleColor = theme.groundSoilColor.copy(alpha = 0.85f).let {
            Color(
                (it.red * 0.82f),
                (it.green * 0.82f),
                (it.blue * 0.82f),
                1f
            )
        }
        val soilTileWidth = 28f
        var sx = 0f
        while (sx < size.width) {
            for (r in 0..3) {
                val sy = groundY + 28f + r * 22f
                if (sy < size.height) {
                    drawRect(
                        color = speckleColor,
                        topLeft = Offset(sx + (r * 11) % 18, sy),
                        size = Size(5f, 3f)
                    )
                }
            }
            sx += soilTileWidth
        }

        // 2. Top Grass Band
        val grassHeight = 18f
        drawRect(
            color = theme.groundGrassLight,
            topLeft = Offset(0f, groundY),
            size = Size(size.width, grassHeight)
        )

        // 3. Diagonal Chevron Grass Stripes (scrolling!)
        val stripeWidth = 16f
        val offset = engine.groundOffset % (stripeWidth * 2f)
        var x = -stripeWidth * 2f - offset
        while (x < size.width + stripeWidth * 2f) {
            val stripePath = Path().apply {
                moveTo(x, groundY)
                lineTo(x + stripeWidth, groundY)
                lineTo(x + stripeWidth - 8f, groundY + grassHeight)
                lineTo(x - 8f, groundY + grassHeight)
                close()
            }
            drawPath(stripePath, color = theme.groundGrassDark)
            x += stripeWidth * 2f
        }

        // 4. Ground Top Border Line
        drawLine(
            color = borderColor,
            start = Offset(0f, groundY),
            end = Offset(size.width, groundY),
            strokeWidth = 3f
        )
        drawLine(
            color = borderColor,
            start = Offset(0f, groundY + grassHeight),
            end = Offset(size.width, groundY + grassHeight),
            strokeWidth = 2f
        )
    }

    private fun DrawScope.drawBird(bird: Bird, skin: BirdSkin) {
        val cx = bird.x
        val cy = bird.y
        val r = bird.radius
        val strokeWidth = 2.4f
        val outlineColor = Color(0xFF1E1E1E)

        translate(cx, cy) {
            rotate(bird.rotation) {
                // 1. Tail Feathers (pointing back)
                val tailPath = Path().apply {
                    moveTo(-r * 0.8f, -r * 0.1f)
                    lineTo(-r * 1.35f, -r * 0.35f)
                    lineTo(-r * 1.25f, 0.1f)
                    lineTo(-r * 1.35f, r * 0.35f)
                    lineTo(-r * 0.8f, r * 0.2f)
                    close()
                }
                drawPath(tailPath, color = skin.wingColor)
                drawPath(tailPath, color = outlineColor, style = Stroke(strokeWidth))

                // 2. Main Bird Body (Round Oval)
                val bodyWidth = r * 2.3f
                val bodyHeight = r * 1.95f
                val bodyTopLeft = Offset(-bodyWidth * 0.52f, -bodyHeight * 0.5f)

                drawOval(
                    color = skin.primaryColor,
                    topLeft = bodyTopLeft,
                    size = Size(bodyWidth, bodyHeight)
                )

                // 3. Fluffy Belly / Breast Patch
                val bellyPath = Path().apply {
                    moveTo(-bodyWidth * 0.2f, bodyHeight * 0.45f)
                    quadraticTo(
                        bodyWidth * 0.35f, bodyHeight * 0.42f,
                        bodyWidth * 0.42f, 0f
                    )
                    quadraticTo(
                        0f, bodyHeight * 0.15f,
                        -bodyWidth * 0.2f, bodyHeight * 0.45f
                    )
                    close()
                }
                drawPath(bellyPath, color = skin.bellyColor)

                // Body Outline
                drawOval(
                    color = outlineColor,
                    topLeft = bodyTopLeft,
                    size = Size(bodyWidth, bodyHeight),
                    style = Stroke(strokeWidth)
                )

                // 4. Large Iconic Eye
                val eyeRadiusX = r * 0.48f
                val eyeRadiusY = r * 0.56f
                val eyeCenter = Offset(r * 0.34f, -r * 0.32f)

                // White sclera
                drawOval(
                    color = Color.White,
                    topLeft = Offset(eyeCenter.x - eyeRadiusX, eyeCenter.y - eyeRadiusY),
                    size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f)
                )
                drawOval(
                    color = outlineColor,
                    topLeft = Offset(eyeCenter.x - eyeRadiusX, eyeCenter.y - eyeRadiusY),
                    size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f),
                    style = Stroke(strokeWidth)
                )

                // Black Pupil
                val pupilRadius = eyeRadiusX * 0.52f
                val pupilCenter = Offset(eyeCenter.x + eyeRadiusX * 0.22f, eyeCenter.y)
                drawCircle(
                    color = skin.eyePupilColor,
                    radius = pupilRadius,
                    center = pupilCenter
                )
                // White catchlight gleam
                drawCircle(
                    color = Color.White,
                    radius = pupilRadius * 0.38f,
                    center = Offset(pupilCenter.x + pupilRadius * 0.25f, pupilCenter.y - pupilRadius * 0.3f)
                )

                // 5. Beak / Lips (Big orange pout)
                val beakPath = Path().apply {
                    // Upper lip
                    moveTo(r * 0.48f, -r * 0.08f)
                    quadraticTo(r * 0.95f, -r * 0.22f, r * 1.45f, 0.05f)
                    lineTo(r * 0.55f, 0.15f)
                    close()
                }
                drawPath(beakPath, color = skin.beakColor)
                drawPath(beakPath, color = outlineColor, style = Stroke(strokeWidth))

                val lowerBeakPath = Path().apply {
                    // Lower lip
                    moveTo(r * 0.48f, 0.12f)
                    lineTo(r * 1.35f, 0.08f)
                    quadraticTo(r * 0.95f, r * 0.45f, r * 0.42f, r * 0.32f)
                    close()
                }
                drawPath(lowerBeakPath, color = skin.beakColor.copy(alpha = 0.9f))
                drawPath(lowerBeakPath, color = outlineColor, style = Stroke(strokeWidth))

                // Smile line inside beak
                drawLine(
                    color = outlineColor,
                    start = Offset(r * 0.48f, 0.10f),
                    end = Offset(r * 1.38f, 0.07f),
                    strokeWidth = strokeWidth * 0.9f
                )

                // 6. Cute Rosy Cheek
                drawCircle(
                    color = Color(0xFFFF5252).copy(alpha = 0.45f),
                    radius = r * 0.28f,
                    center = Offset(r * 0.10f, r * 0.18f)
                )

                // 7. Flapping Wing
                val wingYOffset = when (bird.wingFrame) {
                    1 -> r * 0.32f   // Down flap
                    2 -> -r * 0.35f  // Up flap
                    else -> 0f       // Mid
                }
                val wingAngle = when (bird.wingFrame) {
                    1 -> -25f
                    2 -> 35f
                    else -> 0f
                }

                translate(-r * 0.32f, wingYOffset) {
                    rotate(wingAngle) {
                        val wingWidth = r * 1.15f
                        val wingHeight = r * 0.72f
                        val wingTopLeft = Offset(-wingWidth * 0.5f, -wingHeight * 0.5f)

                        drawOval(
                            color = skin.wingColor,
                            topLeft = wingTopLeft,
                            size = Size(wingWidth, wingHeight)
                        )
                        drawOval(
                            color = outlineColor,
                            topLeft = wingTopLeft,
                            size = Size(wingWidth, wingHeight),
                            style = Stroke(strokeWidth)
                        )
                    }
                }
            }
        }
    }

    private fun DrawScope.drawParticles(particles: List<Particle>) {
        for (p in particles) {
            val progress = p.life / p.maxLife
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val currentSize = p.size * (1f - progress * 0.5f)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = currentSize,
                center = Offset(p.x, p.y)
            )
        }
    }
}
