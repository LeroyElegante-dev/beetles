package com.example.beetles.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.beetles.model.Gender
import com.example.beetles.model.Player
import com.example.beetles.model.ZodiacSign

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val gender: String,
    val course: Int,
    val difficulty: Int,
    val birthYear: Int,
    val birthMonth: Int,
    val birthDay: Int,
    val zodiacSign: String
) {
    fun toPlayer(): Player = Player(
        id = id,
        fullName = fullName,
        gender = Gender.valueOf(gender),
        course = course,
        difficulty = difficulty,
        birthYear = birthYear,
        birthMonth = birthMonth,
        birthDay = birthDay,
        zodiacSign = ZodiacSign.valueOf(zodiacSign)
    )

    companion object {
        fun from(player: Player): PlayerEntity = PlayerEntity(
            id = player.id,
            fullName = player.fullName,
            gender = player.gender.name,
            course = player.course,
            difficulty = player.difficulty,
            birthYear = player.birthYear,
            birthMonth = player.birthMonth,
            birthDay = player.birthDay,
            zodiacSign = player.zodiacSign.name
        )
    }
}
