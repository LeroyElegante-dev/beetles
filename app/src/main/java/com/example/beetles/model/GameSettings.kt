package com.example.beetles.model

data class GameSettings(
    val speed: Int,
    val maxBeetles: Int,
    val bonusIntervalSeconds: Int,
    val roundDurationSeconds: Int
) {
    companion object {
        val DEFAULT = GameSettings(
            speed = 5,
            maxBeetles = 10,
            bonusIntervalSeconds = 15,
            roundDurationSeconds = 60
        )
    }
}
