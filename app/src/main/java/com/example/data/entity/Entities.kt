package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val difficulty: String,
    val birdSkinId: String,
    val medalAwarded: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val coins: Int = 0,
    val bestScoreClassic: Int = 0,
    val bestScoreCasual: Int = 0,
    val bestScoreHardcore: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalFlaps: Long = 0L,
    val totalPipesPassed: Long = 0L,
    val unlockedSkinsCsv: String = "faby_yellow",
    val selectedSkinId: String = "faby_yellow",
    val selectedDifficulty: String = "classic",
    val selectedTheme: String = "day",
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true
)
