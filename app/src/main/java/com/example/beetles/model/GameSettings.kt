package com.example.beetles.model

data class GameSettings(
    val speed: Int,
    val maxBeetles: Int,
    val bonusIntervalSeconds: Int,
    val roundDurationSeconds: Int
)
