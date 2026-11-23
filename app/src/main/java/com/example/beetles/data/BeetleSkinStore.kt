package com.example.beetles.data

import android.content.Context
import com.example.beetles.game.InsectKind
import com.example.beetles.model.BeetleModel
import com.example.beetles.model.BeetleMood
import com.example.beetles.model.BeetleSkinCatalog
import com.example.beetles.model.BeetleSkinSet
import com.example.beetles.model.KindLook

class BeetleSkinStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadCatalog(): BeetleSkinCatalog {
        val raw = prefs.getString(KEY_SETS, null)
        val selected = prefs.getString(KEY_SELECTED, BeetleSkinSet.DEFAULT_ID) ?: BeetleSkinSet.DEFAULT_ID
        if (raw.isNullOrBlank()) {
            return BeetleSkinCatalog(BeetleSkinSet.builtIns(), selected)
        }
        val parsed = raw.split('\n').mapNotNull { decode(it) }
        return BeetleSkinCatalog(mergeWithBuiltIns(parsed), selected)
    }

    fun saveCatalog(catalog: BeetleSkinCatalog) {
        prefs.edit()
            .putString(KEY_SETS, catalog.list().joinToString("\n") { encode(it) })
            .putString(KEY_SELECTED, catalog.selectedId)
            .apply()
    }

    fun selected(): BeetleSkinSet = loadCatalog().selected()

    private fun mergeWithBuiltIns(saved: List<BeetleSkinSet>): List<BeetleSkinSet> {
        val byId = saved.filterNot { it.builtIn }.associateBy { it.id }.toMutableMap()
        BeetleSkinSet.builtIns().forEach { builtIn ->
            byId[builtIn.id] = builtIn
        }
        return byId.values.toList()
    }

    private fun encode(set: BeetleSkinSet): String {
        return listOf(
            set.id,
            set.name.replace(SET_SEP, " ").replace(LOOK_SEP, " "),
            set.builtIn.toString(),
            encodeLook(set.normal),
            encodeLook(set.fast),
            encodeLook(set.rare),
            encodeLook(set.golden)
        ).joinToString(SET_SEP)
    }

    private fun decode(line: String): BeetleSkinSet? {
        val parts = line.split(SET_SEP)
        if (parts.size < 7) return null
        return runCatching {
            BeetleSkinSet(
                id = parts[0],
                name = parts[1],
                builtIn = parts[2].toBooleanStrictOrNull() ?: false,
                normal = decodeLook(parts[3], InsectKind.NORMAL),
                fast = decodeLook(parts[4], InsectKind.FAST),
                rare = decodeLook(parts[5], InsectKind.RARE),
                golden = decodeLook(parts[6], InsectKind.GOLDEN)
            ).normalized()
        }.getOrNull()
    }

    private fun encodeLook(look: KindLook): String {
        return listOf(
            look.model.name,
            look.bodyColor.toString(),
            look.accentColor.toString(),
            look.eyeColor.toString(),
            look.sizeScale.toString(),
            look.mood.name
        ).joinToString(LOOK_SEP)
    }

    private fun decodeLook(raw: String, kind: InsectKind): KindLook {
        val parts = raw.split(LOOK_SEP)
        if (parts.size < 6) return BeetleModel.defaultFor(kind).defaultLook()
        return KindLook(
            model = BeetleModel.fromName(parts[0], kind),
            bodyColor = parts[1].toInt(),
            accentColor = parts[2].toInt(),
            eyeColor = parts[3].toInt(),
            sizeScale = parts[4].toFloat(),
            mood = BeetleMood.valueOf(parts[5])
        ).normalized()
    }

    private companion object {
        const val PREFS_NAME = "beetle_skins_v2"
        const val KEY_SETS = "sets"
        const val KEY_SELECTED = "selected_id"
        const val SET_SEP = "|"
        const val LOOK_SEP = ";"
    }
}
