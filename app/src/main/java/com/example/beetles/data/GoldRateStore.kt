package com.example.beetles.data

import android.content.Context

class GoldRateStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRate(): Double = Double.fromBits(prefs.getLong(KEY_RATE_BITS, Double.NaN.toRawBits()))

    fun getRateOrDefault(): Double {
        val rate = getRate()
        return if (rate.isNaN() || rate <= 0.0) DEFAULT_RATE else rate
    }

    fun getDate(): String = prefs.getString(KEY_DATE, "") ?: ""

    fun save(rate: Double, date: String) {
        prefs.edit()
            .putLong(KEY_RATE_BITS, rate.toRawBits())
            .putString(KEY_DATE, date)
            .apply()
    }

    fun pointsForHit(): Int {
        return (getRateOrDefault() / POINTS_DIVISOR).toInt().coerceAtLeast(1)
    }

    companion object {
        const val DEFAULT_RATE = 8000.0
        const val POINTS_DIVISOR = 100.0
        private const val PREFS_NAME = "gold_rate"
        private const val KEY_RATE_BITS = "rate_bits"
        private const val KEY_DATE = "date"
    }
}
