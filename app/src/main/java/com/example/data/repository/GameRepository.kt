package com.example.data.repository

import com.example.data.dao.GameDao
import com.example.data.entity.ScoreRecord
import com.example.data.entity.UserProfile
import com.example.data.model.MedalType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GameRepository(private val dao: GameDao) {

    val userProfile: Flow<UserProfile> = dao.getUserProfile().map { profile ->
        profile ?: UserProfile()
    }

    val topScores: Flow<List<ScoreRecord>> = dao.getTopScores()

    suspend fun getOrCreateProfile(): UserProfile = withContext(Dispatchers.IO) {
        val existing = dao.getUserProfileOnce()
        if (existing == null) {
            val initial = UserProfile()
            dao.insertOrUpdateProfile(initial)
            initial
        } else {
            existing
        }
    }

    suspend fun recordGameFinished(
        score: Int,
        difficulty: String,
        birdSkinId: String,
        coinsEarned: Int,
        flapsCount: Long,
        pipesPassedCount: Long
    ): Pair<UserProfile, Boolean> = withContext(Dispatchers.IO) {
        val currentProfile = getOrCreateProfile()

        val isNewBest = when (difficulty) {
            "casual" -> score > currentProfile.bestScoreCasual
            "hardcore" -> score > currentProfile.bestScoreHardcore
            else -> score > currentProfile.bestScoreClassic
        }

        val newBestCasual = if (difficulty == "casual" && score > currentProfile.bestScoreCasual) score else currentProfile.bestScoreCasual
        val newBestClassic = if (difficulty == "classic" && score > currentProfile.bestScoreClassic) score else currentProfile.bestScoreClassic
        val newBestHardcore = if (difficulty == "hardcore" && score > currentProfile.bestScoreHardcore) score else currentProfile.bestScoreHardcore

        val medal = MedalType.fromScore(score)
        if (score > 0) {
            dao.insertScore(
                ScoreRecord(
                    score = score,
                    difficulty = difficulty,
                    birdSkinId = birdSkinId,
                    medalAwarded = medal.name
                )
            )
        }

        val updated = currentProfile.copy(
            coins = currentProfile.coins + coinsEarned,
            bestScoreCasual = newBestCasual,
            bestScoreClassic = newBestClassic,
            bestScoreHardcore = newBestHardcore,
            totalGamesPlayed = currentProfile.totalGamesPlayed + 1,
            totalFlaps = currentProfile.totalFlaps + flapsCount,
            totalPipesPassed = currentProfile.totalPipesPassed + pipesPassedCount
        )

        dao.insertOrUpdateProfile(updated)
        Pair(updated, isNewBest)
    }

    suspend fun unlockSkin(skinId: String, cost: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        val currentSkins = profile.unlockedSkinsCsv.split(",").map { it.trim() }.toMutableSet()
        if (currentSkins.contains(skinId)) {
            // Already unlocked, just select
            dao.insertOrUpdateProfile(profile.copy(selectedSkinId = skinId))
            return@withContext true
        }

        if (profile.coins >= cost) {
            currentSkins.add(skinId)
            val updated = profile.copy(
                coins = profile.coins - cost,
                unlockedSkinsCsv = currentSkins.joinToString(","),
                selectedSkinId = skinId
            )
            dao.insertOrUpdateProfile(updated)
            true
        } else {
            false
        }
    }

    suspend fun selectSkin(skinId: String) = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        dao.insertOrUpdateProfile(profile.copy(selectedSkinId = skinId))
    }

    suspend fun updateSettings(
        sound: Boolean,
        vibrate: Boolean,
        difficulty: String,
        theme: String
    ) = withContext(Dispatchers.IO) {
        val profile = getOrCreateProfile()
        val updated = profile.copy(
            soundEnabled = sound,
            vibrateEnabled = vibrate,
            selectedDifficulty = difficulty,
            selectedTheme = theme
        )
        dao.insertOrUpdateProfile(updated)
    }
}
