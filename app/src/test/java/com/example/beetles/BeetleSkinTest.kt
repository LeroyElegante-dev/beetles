package com.example.beetles

import com.example.beetles.game.InsectKind
import com.example.beetles.model.BeetleModel
import com.example.beetles.model.BeetleMood
import com.example.beetles.model.BeetleSkinSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BeetleSkinTest {

    @Test
    fun clampSize_keepsValueInsideRange() {
        assertEquals(BeetleSkinSet.MIN_SIZE_SCALE, BeetleSkinSet.clampSize(0.1f), 0.0001f)
        assertEquals(BeetleSkinSet.MAX_SIZE_SCALE, BeetleSkinSet.clampSize(4f), 0.0001f)
        assertEquals(1.1f, BeetleSkinSet.clampSize(1.1f), 0.0001f)
    }

    @Test
    fun sizeProgress_roundTripsThroughSeekBar() {
        val original = 1.2f
        val progress = BeetleSkinSet.sizeProgress(original)
        val restored = BeetleSkinSet.sizeFromProgress(progress)
        assertEquals(original, restored, 0.0001f)
    }

    @Test
    fun scaledSize_usesLookOfThatKind() {
        val look = BeetleModel.BEETLE_RHINO.defaultLook().copy(sizeScale = 1.5f)
        assertEquals(150f, BeetleSkinSet.scaledSize(100f, look), 0.0001f)
    }

    @Test
    fun eachKind_hasThreeReadyModels() {
        InsectKind.entries.forEach { kind ->
            val models = BeetleModel.forKind(kind)
            assertEquals(3, models.size)
            assertTrue(models.all { it.kind == kind })
        }
    }

    @Test
    fun fromName_rejectsModelOfAnotherKind() {
        val restored = BeetleModel.fromName(BeetleModel.LADY_HEART.name, InsectKind.NORMAL)
        assertEquals(BeetleModel.BEETLE_CLASSIC, restored)
    }

    @Test
    fun withLook_keepsSeparateModelsPerKind() {
        val set = BeetleSkinSet.DEFAULT.withLook(
            InsectKind.FAST,
            BeetleModel.ROACH_RACER.defaultLook().copy(
                bodyColor = 0xFF000000.toInt(),
                mood = BeetleMood.EVIL,
                sizeScale = 9f
            )
        ).normalized()
        assertEquals(BeetleModel.BEETLE_CLASSIC, set.normal.model)
        assertEquals(BeetleModel.ROACH_RACER, set.fast.model)
        assertEquals(BeetleMood.EVIL, set.fast.mood)
        assertEquals(BeetleSkinSet.MAX_SIZE_SCALE, set.fast.sizeScale, 0.0001f)
        assertEquals(BeetleModel.LADY_CLASSIC, set.rare.model)
        assertEquals(BeetleModel.GOLD_CLASSIC, set.golden.model)
    }
}
