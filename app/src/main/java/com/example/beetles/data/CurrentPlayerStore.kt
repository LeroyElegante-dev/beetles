package com.example.beetles.data

import android.content.Context

class CurrentPlayerStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPlayerId(): Long = prefs.getLong(KEY_PLAYER_ID, NO_PLAYER)

    fun setPlayerId(id: Long) {
        prefs.edit().putLong(KEY_PLAYER_ID, id).apply()
    }

    fun hasPlayer(): Boolean = getPlayerId() != NO_PLAYER

    companion object {
        const val NO_PLAYER = -1L
        private const val PREFS_NAME = "current_player"
        private const val KEY_PLAYER_ID = "player_id"
    }
}
