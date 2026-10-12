package com.example.beetles.data

import android.content.Context
import com.example.beetles.model.GameSettings

class GameSettingsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): GameSettings {
        val defaults = GameSettings.DEFAULT
        return GameSettings(
            speed = prefs.getInt(KEY_SPEED, defaults.speed),
            maxBeetles = prefs.getInt(KEY_MAX_BEETLES, defaults.maxBeetles),
            bonusIntervalSeconds = prefs.getInt(KEY_BONUS_INTERVAL, defaults.bonusIntervalSeconds),
            roundDurationSeconds = prefs.getInt(KEY_ROUND_DURATION, defaults.roundDurationSeconds)
        )
    }

    fun save(settings: GameSettings) {
        prefs.edit()
            .putInt(KEY_SPEED, settings.speed)
            .putInt(KEY_MAX_BEETLES, settings.maxBeetles)
            .putInt(KEY_BONUS_INTERVAL, settings.bonusIntervalSeconds)
            .putInt(KEY_ROUND_DURATION, settings.roundDurationSeconds)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "game_settings"
        const val KEY_SPEED = "speed"
        const val KEY_MAX_BEETLES = "max_beetles"
        const val KEY_BONUS_INTERVAL = "bonus_interval"
        const val KEY_ROUND_DURATION = "round_duration"
    }
}