package com.example.beetles.data

import com.example.beetles.network.CbrClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoldRateRepository(
    private val store: GoldRateStore
) {
    suspend fun refresh(): Result<Double> = runCatching {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val to = Calendar.getInstance()
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -14) }
        val response = CbrClient.api.getMetals(
            from = format.format(from.time),
            to = format.format(to.time)
        )
        val gold = response.records
            ?.asReversed()
            ?.firstOrNull { it.code == GOLD_CODE && !it.buy.isNullOrBlank() }
            ?: error("Gold rate not found in CBR response")
        val rate = parseRuNumber(gold.buy!!)
        store.save(rate, gold.date.orEmpty())
        rate
    }

    suspend fun refreshOrCached(): Double {
        refresh().onSuccess { return it }
        return store.getRateOrDefault()
    }

    private fun parseRuNumber(raw: String): Double {
        return raw.trim().replace(" ", "").replace(',', '.').toDouble()
    }

    companion object {
        const val GOLD_CODE = "1"
    }
}
