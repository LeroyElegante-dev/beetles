package com.example.beetles.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT * FROM scores ORDER BY score DESC, createdAt ASC")
    suspend fun getAllOrdered(): List<ScoreEntity>
}
