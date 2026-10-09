package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class MedalType(val displayName: String, val minScore: Int, val primaryColor: Color, val accentColor: Color) {
    NONE("No Medal", 0, Color.Transparent, Color.Transparent),
    BRONZE("Bronze", 10, Color(0xFFCD7F32), Color(0xFFE8A869)),
    SILVER("Silver", 20, Color(0xFFC0C0C0), Color(0xFFE8E8E8)),
    GOLD("Gold", 30, Color(0xFFFFD700), Color(0xFFFFF275)),
    PLATINUM("Platinum", 40, Color(0xFFE5E4E2), Color(0xFF70D6FF));

    companion object {
        fun fromScore(score: Int): MedalType = when {
            score >= PLATINUM.minScore -> PLATINUM
            score >= GOLD.minScore -> GOLD
            score >= SILVER.minScore -> SILVER
            score >= BRONZE.minScore -> BRONZE
            else -> NONE
        }
    }
}

enum class GameDifficulty(
    val id: String,
    val title: String,
    val description: String,
    val baseSpeed: Float,
    val gapHeightDp: Float,
    val spawnDistanceRatio: Float,
    val gravityFactor: Float
) {
    CASUAL(
        id = "casual",
        title = "Casual",
        description = "Wider gaps, gentler speed. Great for warming up!",
        baseSpeed = 190f,
        gapHeightDp = 220f,
        spawnDistanceRatio = 0.58f,
        gravityFactor = 0.90f
    ),
    CLASSIC(
        id = "classic",
        title = "Classic",
        description = "The authentic, legendary Flappy challenge.",
        baseSpeed = 245f,
        gapHeightDp = 175f,
        spawnDistanceRatio = 0.50f,
        gravityFactor = 1.0f
    ),
    HARDCORE(
        id = "hardcore",
        title = "Hardcore",
        description = "Narrow pipes, higher velocity, maximum adrenaline!",
        baseSpeed = 310f,
        gapHeightDp = 145f,
        spawnDistanceRatio = 0.44f,
        gravityFactor = 1.12f
    );

    companion object {
        fun fromId(id: String): GameDifficulty = entries.find { it.id == id } ?: CLASSIC
    }
}

enum class WorldTheme(
    val id: String,
    val title: String,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val pipeBodyLight: Color,
    val pipeBodyDark: Color,
    val pipeRimLight: Color,
    val pipeRimDark: Color,
    val groundGrassLight: Color,
    val groundGrassDark: Color,
    val groundSoilColor: Color,
    val isNight: Boolean
) {
    DAY(
        id = "day",
        title = "Sunny Day",
        skyTopColor = Color(0xFF4EC0CA),
        skyBottomColor = Color(0xFFB4E9EC),
        pipeBodyLight = Color(0xFF73BF2E),
        pipeBodyDark = Color(0xFF558022),
        pipeRimLight = Color(0xFF98E843),
        pipeRimDark = Color(0xFF436618),
        groundGrassLight = Color(0xFF73BF2E),
        groundGrassDark = Color(0xFF558022),
        groundSoilColor = Color(0xFFDED895),
        isNight = false
    ),
    NIGHT(
        id = "night",
        title = "Midnight",
        skyTopColor = Color(0xFF0C1B33),
        skyBottomColor = Color(0xFF1E3A5F),
        pipeBodyLight = Color(0xFF2E7DBF),
        pipeBodyDark = Color(0xFF1C4D78),
        pipeRimLight = Color(0xFF51A4EC),
        pipeRimDark = Color(0xFF133654),
        groundGrassLight = Color(0xFF2C6340),
        groundGrassDark = Color(0xFF183824),
        groundSoilColor = Color(0xFF786C5A),
        isNight = true
    ),
    SUNSET(
        id = "sunset",
        title = "Golden Sunset",
        skyTopColor = Color(0xFFE26D5C),
        skyBottomColor = Color(0xFFFFB085),
        pipeBodyLight = Color(0xFF8B5A2B),
        pipeBodyDark = Color(0xFF5A391A),
        pipeRimLight = Color(0xFFB57C45),
        pipeRimDark = Color(0xFF3F2710),
        groundGrassLight = Color(0xFFB37D2E),
        groundGrassDark = Color(0xFF7E541C),
        groundSoilColor = Color(0xFFD9B986),
        isNight = false
    );

    companion object {
        fun fromId(id: String): WorldTheme = entries.find { it.id == id } ?: DAY
    }
}

data class BirdSkin(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val bellyColor: Color,
    val wingColor: Color,
    val beakColor: Color,
    val eyePupilColor: Color,
    val unlockPrice: Int,
    val requiredScore: Int = 0
) {
    companion object {
        val ALL_SKINS = listOf(
            BirdSkin(
                id = "faby_yellow",
                name = "Classic Faby",
                description = "The one and only legendary flapper!",
                primaryColor = Color(0xFFF7D02C),
                bellyColor = Color(0xFFFFF275),
                wingColor = Color(0xFFE5B518),
                beakColor = Color(0xFFF96024),
                eyePupilColor = Color(0xFF1E1E1E),
                unlockPrice = 0
            ),
            BirdSkin(
                id = "blue_jay",
                name = "Blue Jet",
                description = "Aerodynamic supersonic azure flyer.",
                primaryColor = Color(0xFF29B6F6),
                bellyColor = Color(0xFFB3E5FC),
                wingColor = Color(0xFF0288D1),
                beakColor = Color(0xFFFF8F00),
                eyePupilColor = Color(0xFF0D47A1),
                unlockPrice = 25
            ),
            BirdSkin(
                id = "crimson_fire",
                name = "Crimson Phoenix",
                description = "Forged in the fires of near-misses.",
                primaryColor = Color(0xFFE53935),
                bellyColor = Color(0xFFFFCDD2),
                wingColor = Color(0xFFB71C1C),
                beakColor = Color(0xFFFFB300),
                eyePupilColor = Color(0xFF3E2723),
                unlockPrice = 50
            ),
            BirdSkin(
                id = "shadow_bat",
                name = "Night Falcon",
                description = "Silent, sleek, and mysterious hunter of dusk.",
                primaryColor = Color(0xFF37474F),
                bellyColor = Color(0xFF78909C),
                wingColor = Color(0xFF212121),
                beakColor = Color(0xFFFF7043),
                eyePupilColor = Color(0xFFE0E0E0),
                unlockPrice = 80
            ),
            BirdSkin(
                id = "golden_champion",
                name = "Golden King",
                description = "Solid gold royalty. The mark of a true flapper!",
                primaryColor = Color(0xFFFFD700),
                bellyColor = Color(0xFFFFF8E1),
                wingColor = Color(0xFFFFA000),
                beakColor = Color(0xFFFF5722),
                eyePupilColor = Color(0xFFBF360C),
                unlockPrice = 120,
                requiredScore = 20
            )
        )

        fun getById(id: String): BirdSkin = ALL_SKINS.find { it.id == id } ?: ALL_SKINS[0]
    }
}
