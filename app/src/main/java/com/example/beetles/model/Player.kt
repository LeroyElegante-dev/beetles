package com.example.beetles.model

data class Player(
    val fullName: String,
    val gender: Gender,
    val course: Int,
    val difficulty: Int,
    val birthYear: Int,
    val birthMonth: Int,
    val birthDay: Int,
    val zodiacSign: ZodiacSign
)
