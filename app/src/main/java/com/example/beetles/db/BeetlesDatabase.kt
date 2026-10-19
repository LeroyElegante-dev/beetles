package com.example.beetles.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PlayerEntity::class, ScoreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BeetlesDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var instance: BeetlesDatabase? = null

        fun get(context: Context): BeetlesDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BeetlesDatabase::class.java,
                    "beetles.db"
                ).build().also { instance = it }
            }
        }
    }
}
