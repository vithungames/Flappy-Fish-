package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedalType

@Composable
fun GameOverDialog(
    score: Int,
    bestScore: Int,
    isNewBest: Boolean,
    coinsEarned: Int,
    medal: MedalType,
    onRestart: () -> Unit,
    onOpenSkins: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenDifficulty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "game_over_anims")
    val newBadgePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "GAME OVER" Banner
        Box(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE53935), Color(0xFFB71C1C))
                    )
                )
                .border(3.dp, Color.White, RoundedCornerShape(14.dp))
                .padding(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Iconic Retro Scoreboard Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFDED895), Color(0xFFC7B874))
                    )
                )
                .border(3.5.dp, Color(0xFF5A4426), RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Medal Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = "MEDAL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7A5826)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    MedalBadge(medal = medal)
                }

                // Right: Scores
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    // Current Score
                    Text(
                        text = "SCORE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7A5826)
                    )
                    Text(
                        text = "${animatedScore.value.toInt()}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF261C0E)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Best Score with optional NEW badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isNewBest && score > 0) {
                            Box(
                                modifier = Modifier
                                    .scale(newBadgePulse)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE53935))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NEW!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "BEST",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7A5826)
                        )
                    }

                    Text(
                        text = "$bestScore",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF261C0E)
                    )

                    if (coinsEarned > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+$coinsEarned Coins",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5A4426)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Action: BIG PLAY AGAIN BUTTON
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(60.dp)
                .shadow(10.dp, RoundedCornerShape(30.dp))
                .testTag("play_again_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF73BF2E)
            ),
            shape = RoundedCornerShape(30.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY AGAIN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Secondary Utility Buttons Row (Skins, Stats, Themes, Difficulty)
        Row(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundMenuButton(
                icon = Icons.Default.Checkroom,
                contentDescription = "Bird Skins",
                backgroundColor = Color(0xFF29B6F6),
                onClick = onOpenSkins,
                testTag = "skins_button"
            )
            RoundMenuButton(
                icon = Icons.Default.BarChart,
                contentDescription = "Stats & Records",
                backgroundColor = Color(0xFFFFB300),
                onClick = onOpenStats,
                testTag = "stats_button"
            )
            RoundMenuButton(
                icon = Icons.Default.Palette,
                contentDescription = "Themes",
                backgroundColor = Color(0xFFAB47BC),
                onClick = onOpenThemes,
                testTag = "themes_button"
            )
            RoundMenuButton(
                icon = Icons.Default.Speed,
                contentDescription = "Difficulty",
                backgroundColor = Color(0xFFEF5350),
                onClick = onOpenDifficulty,
                testTag = "difficulty_button"
            )
        }
    }
}

@Composable
fun MedalBadge(medal: MedalType, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "medal_gleam")
    val sparkleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gleam"
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(
                if (medal != MedalType.NONE) {
                    Brush.radialGradient(
                        listOf(medal.accentColor, medal.primaryColor, Color(0xFF2E2010))
                    )
                } else {
                    Brush.radialGradient(
                        listOf(Color(0xFF8D7F6F), Color(0xFF5D5449))
                    )
                }
            )
            .border(3.dp, Color(0xFF5A4426), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (medal != MedalType.NONE) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = medal.displayName,
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.size(38.dp)
            )

            // Sparkle overlay
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = 18.dp, y = (-16).dp)
                    .rotate(sparkleAngle)
            )
        } else {
            Text(
                text = "---",
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun RoundMenuButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
        modifier = modifier
            .size(52.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
