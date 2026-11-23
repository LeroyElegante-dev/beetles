package com.example.beetles.model

class BeetleSkinCatalog(
    sets: List<BeetleSkinSet> = BeetleSkinSet.builtIns(),
    selectedId: String = BeetleSkinSet.DEFAULT_ID
) {
    private val items = linkedMapOf<String, BeetleSkinSet>()

    var selectedId: String
        private set

    init {
        val source = if (sets.isEmpty()) BeetleSkinSet.builtIns() else sets
        source.forEach { set ->
            val normalized = set.normalized()
            if (normalized.name.isNotEmpty()) {
                items[normalized.id] = normalized
            }
        }
        if (items.isEmpty()) {
            items[BeetleSkinSet.DEFAULT.id] = BeetleSkinSet.DEFAULT
        }
        this.selectedId = if (items.containsKey(selectedId)) selectedId else items.keys.first()
    }

    fun list(): List<BeetleSkinSet> = items.values.toList()

    fun selected(): BeetleSkinSet = items[selectedId] ?: BeetleSkinSet.DEFAULT

    fun select(id: String): Boolean {
        if (!items.containsKey(id)) return false
        selectedId = id
        return true
    }

    fun save(draft: BeetleSkinSet): SaveResult {
        val normalized = draft.normalized()
        if (normalized.name.isEmpty()) {
            return SaveResult.EmptyName
        }
        val existingByName = items.values.firstOrNull {
            it.name.equals(normalized.name, ignoreCase = true) && it.id != normalized.id
        }
        if (existingByName != null && existingByName.builtIn) {
            return SaveResult.NameTaken
        }
        val id = when {
            normalized.id.isNotBlank() && items[normalized.id]?.builtIn == true -> newId()
            normalized.id.isNotBlank() -> normalized.id
            else -> newId()
        }
        val stored = normalized.copy(id = id, builtIn = false)
        items[id] = stored
        selectedId = id
        return SaveResult.Ok(stored)
    }

    fun delete(id: String): Boolean {
        val set = items[id] ?: return false
        if (set.builtIn) return false
        if (items.size <= 1) return false
        items.remove(id)
        if (selectedId == id) {
            selectedId = if (items.containsKey(BeetleSkinSet.DEFAULT_ID)) {
                BeetleSkinSet.DEFAULT_ID
            } else {
                items.keys.first()
            }
        }
        return true
    }

    private fun newId(): String = "custom_${System.currentTimeMillis()}_${items.size}"

    sealed interface SaveResult {
        data class Ok(val set: BeetleSkinSet) : SaveResult
        data object EmptyName : SaveResult
        data object NameTaken : SaveResult
    }
}
