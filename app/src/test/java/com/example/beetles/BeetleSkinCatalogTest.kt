package com.example.beetles

import com.example.beetles.game.InsectKind
import com.example.beetles.model.BeetleModel
import com.example.beetles.model.BeetleMood
import com.example.beetles.model.BeetleSkinCatalog
import com.example.beetles.model.BeetleSkinSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeetleSkinCatalogTest {

    @Test
    fun save_customSet_keepsDifferentModelsPerKind() {
        val catalog = BeetleSkinCatalog()
        val draft = BeetleSkinSet.DEFAULT.copy(
            id = "",
            name = "Мой набор",
            builtIn = false,
            normal = BeetleModel.BEETLE_STAG.defaultLook(),
            fast = BeetleModel.ROACH_RACER.defaultLook().copy(mood = BeetleMood.EVIL),
            rare = BeetleModel.LADY_HEART.defaultLook(),
            golden = BeetleModel.GOLD_JEWEL.defaultLook()
        )
        val result = catalog.save(draft)
        assertTrue(result is BeetleSkinCatalog.SaveResult.Ok)
        val saved = (result as BeetleSkinCatalog.SaveResult.Ok).set
        assertEquals("Мой набор", catalog.selected().name)
        assertEquals(saved.id, catalog.selectedId)
        assertEquals(BeetleModel.BEETLE_STAG, catalog.selected().look(InsectKind.NORMAL).model)
        assertEquals(BeetleModel.ROACH_RACER, catalog.selected().look(InsectKind.FAST).model)
        assertEquals(BeetleMood.EVIL, catalog.selected().look(InsectKind.FAST).mood)
        assertEquals(BeetleModel.LADY_HEART, catalog.selected().look(InsectKind.RARE).model)
        assertEquals(BeetleModel.GOLD_JEWEL, catalog.selected().look(InsectKind.GOLDEN).model)
        assertFalse(catalog.selected().builtIn)
    }

    @Test
    fun save_emptyName_isRejected() {
        val catalog = BeetleSkinCatalog()
        val result = catalog.save(BeetleSkinSet.DEFAULT.copy(id = "", name = "   ", builtIn = false))
        assertEquals(BeetleSkinCatalog.SaveResult.EmptyName, result)
        assertEquals(BeetleSkinSet.DEFAULT_ID, catalog.selectedId)
    }

    @Test
    fun delete_builtIn_isRejected() {
        val catalog = BeetleSkinCatalog()
        assertFalse(catalog.delete(BeetleSkinSet.DEFAULT_ID))
        assertEquals(3, catalog.list().size)
    }

    @Test
    fun delete_custom_fallsBackToDefault() {
        val catalog = BeetleSkinCatalog()
        val saved = (catalog.save(
            BeetleSkinSet.DEFAULT.copy(id = "", name = "Временный", builtIn = false)
        ) as BeetleSkinCatalog.SaveResult.Ok).set
        assertTrue(catalog.delete(saved.id))
        assertEquals(BeetleSkinSet.DEFAULT_ID, catalog.selectedId)
        assertTrue(catalog.list().none { it.id == saved.id })
    }

    @Test
    fun select_unknownId_keepsPrevious() {
        val catalog = BeetleSkinCatalog()
        assertFalse(catalog.select("missing"))
        assertEquals(BeetleSkinSet.DEFAULT_ID, catalog.selectedId)
    }
}
