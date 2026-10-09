package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.GameDao
import com.example.data.entity.ScoreRecord
import com.example.data.entity.UserProfile

@Database(
    entities = [ScoreRecord::class, UserProfile::class],
    version = 1,
    exportSchema = false
)
abstract class FlappyDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: FlappyDatabase? = null

        fun getInstance(context: Context): FlappyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlappyDatabase::class.java,
                    "flappy_bird_master.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
