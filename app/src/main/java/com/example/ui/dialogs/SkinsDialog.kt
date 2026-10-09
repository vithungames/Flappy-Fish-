package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BirdSkin
import com.example.game.Bird
import com.example.ui.game.GameRenderer

@Composable
fun SkinsDialog(
    userCoins: Int,
    unlockedSkinsCsv: String,
    selectedSkinId: String,
    onSelectSkin: (BirdSkin) -> Unit,
    onUnlockSkin: (BirdSkin, () -> Unit, () -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val unlockedSet = remember(unlockedSkinsCsv) {
        unlockedSkinsCsv.split(",").map { it.trim() }.toSet()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BIRD WARDROBE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$userCoins Coins available",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("close_skins_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skins List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(BirdSkin.ALL_SKINS) { skin ->
                        val isUnlocked = unlockedSet.contains(skin.id)
                        val isSelected = selectedSkinId == skin.id

                        SkinItemCard(
                            skin = skin,
                            isUnlocked = isUnlocked,
                            isSelected = isSelected,
                            userCoins = userCoins,
                            onSelect = { onSelectSkin(skin) },
                            onUnlock = {
                                onUnlockSkin(
                                    skin,
                                    { Toast.makeText(context, "Unlocked ${skin.name}!", Toast.LENGTH_SHORT).show() },
                                    { Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkinItemCard(
    skin: BirdSkin,
    isUnlocked: Boolean,
    isSelected: Boolean,
    userCoins: Int,
    onSelect: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2C3E50) else Color(0xFF161F2E)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF73BF2E) else Color(0x44FFFFFF)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature Bird Preview Canvas
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1722))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(54.dp)) {
                    val previewBird = Bird(
                        x = size.width * 0.45f,
                        y = size.height * 0.5f,
                        radius = 18f,
                        wingFrame = 0,
                        rotation = 0f
                    )
                    // Draw preview bird
                    with(GameRenderer) {
                        // Drawing just bird through canvas scope
                    }
                    drawCircle(
                        color = skin.primaryColor,
                        radius = 14f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.45f, size.height * 0.5f)
                    )
                    // Eye
                    drawCircle(
                        color = Color.White,
                        radius = 5.5f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.62f, size.height * 0.42f)
                    )
                    drawCircle(
                        color = skin.eyePupilColor,
                        radius = 2.8f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.42f)
                    )
                    // Beak
                    drawCircle(
                        color = skin.beakColor,
                        radius = 4.5f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.54f)
                    )
                    // Wing
                    drawOval(
                        color = skin.wingColor,
                        topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height * 0.44f),
                        size = androidx.compose.ui.geometry.Size(14f, 10f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = skin.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = skin.description,
                    color = Color(0xFFA0B0C0),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            when {
                isSelected -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF73BF2E))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                isUnlocked -> {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "EQUIP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                else -> {
                    val canAfford = userCoins >= skin.unlockPrice
                    Button(
                        onClick = onUnlock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFFFFB300) else Color(0xFF5A4426)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${skin.unlockPrice}C",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
