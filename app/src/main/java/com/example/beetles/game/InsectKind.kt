package com.example.beetles.game

import com.example.beetles.R

enum class InsectKind(
    val drawableRes: Int,
    val points: Int,
    val sizeMultiplier: Float,
    val speedMultiplier: Float,
    val spawnChance: Int
) {
    NORMAL(R.drawable.ic_insect_beetle, 10, 1f, 1f, 60),
    FAST(R.drawable.ic_insect_cockroach, 15, 0.65f, 2.1f, 30),
    RARE(R.drawable.ic_insect_ladybug, 40, 1.25f, 0.7f, 10),
    GOLDEN(R.drawable.ic_insect_golden_cockroach, 0, 0.85f, 1.6f, 0);

    companion object {
        fun random(random: kotlin.random.Random): InsectKind {
            val pool = entries.filter { it.spawnChance > 0 }
            val roll = random.nextInt(100)
            var threshold = 0
            pool.forEach { kind ->
                threshold += kind.spawnChance
                if (roll < threshold) return kind
            }
            return NORMAL
        }
    }
}
