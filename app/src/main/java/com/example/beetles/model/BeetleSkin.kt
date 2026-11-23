package com.example.beetles.model

import com.example.beetles.game.InsectKind

enum class BeetleMood {
    KIND,
    EVIL
}

enum class BeetleModel(
    val kind: InsectKind,
    val title: String,
    val defaultBody: Int,
    val defaultAccent: Int,
    val defaultEye: Int
) {
    BEETLE_CLASSIC(InsectKind.NORMAL, "Жук", 0xFF6D4C41.toInt(), 0xFF3E2723.toInt(), 0xFFFFCC80.toInt()),
    BEETLE_RHINO(InsectKind.NORMAL, "Носорог", 0xFF455A64.toInt(), 0xFF263238.toInt(), 0xFFFFE082.toInt()),
    BEETLE_STAG(InsectKind.NORMAL, "Олень", 0xFF4E342E.toInt(), 0xFF1B0000.toInt(), 0xFFFFAB91.toInt()),

    ROACH_CLASSIC(InsectKind.FAST, "Таракан", 0xFF3E2723.toInt(), 0xFF4E342E.toInt(), 0xFFFFCC80.toInt()),
    ROACH_RACER(InsectKind.FAST, "Гонщик", 0xFF212121.toInt(), 0xFFFF6F00.toInt(), 0xFFFFF59D.toInt()),
    ROACH_ARMOR(InsectKind.FAST, "Броня", 0xFF37474F.toInt(), 0xFF90A4AE.toInt(), 0xFFB0BEC5.toInt()),

    LADY_CLASSIC(InsectKind.RARE, "Коровка", 0xFFC62828.toInt(), 0xFF212121.toInt(), 0xFFECEFF1.toInt()),
    LADY_HEART(InsectKind.RARE, "Сердечко", 0xFFE91E63.toInt(), 0xFF880E4F.toInt(), 0xFFFFF8E1.toInt()),
    LADY_ROYAL(InsectKind.RARE, "Королева", 0xFF6A1B9A.toInt(), 0xFFFFD54F.toInt(), 0xFFFFFDE7.toInt()),

    GOLD_CLASSIC(InsectKind.GOLDEN, "Золотой", 0xFFFFD54F.toInt(), 0xFFFF8F00.toInt(), 0xFFFFF8E1.toInt()),
    GOLD_SCARAB(InsectKind.GOLDEN, "Скарабей", 0xFFFFC107.toInt(), 0xFF1B5E20.toInt(), 0xFFE8F5E9.toInt()),
    GOLD_JEWEL(InsectKind.GOLDEN, "Самоцвет", 0xFFFFB300.toInt(), 0xFF00ACC1.toInt(), 0xFFE0F7FA.toInt());

    fun defaultLook(): KindLook = KindLook(
        model = this,
        bodyColor = defaultBody,
        accentColor = defaultAccent,
        eyeColor = defaultEye,
        sizeScale = 1f,
        mood = BeetleMood.KIND
    )

    companion object {
        fun forKind(kind: InsectKind): List<BeetleModel> = entries.filter { it.kind == kind }

        fun defaultFor(kind: InsectKind): BeetleModel = forKind(kind).first()

        fun fromName(raw: String?, kind: InsectKind): BeetleModel {
            val found = entries.firstOrNull { it.name == raw }
            return if (found != null && found.kind == kind) found else defaultFor(kind)
        }
    }
}

data class KindLook(
    val model: BeetleModel,
    val bodyColor: Int,
    val accentColor: Int,
    val eyeColor: Int,
    val sizeScale: Float,
    val mood: BeetleMood
) {
    fun normalized(): KindLook = copy(sizeScale = BeetleSkinSet.clampSize(sizeScale))
}

data class BeetleSkinSet(
    val id: String,
    val name: String,
    val builtIn: Boolean = false,
    val normal: KindLook,
    val fast: KindLook,
    val rare: KindLook,
    val golden: KindLook
) {
    fun look(kind: InsectKind): KindLook = when (kind) {
        InsectKind.NORMAL -> normal
        InsectKind.FAST -> fast
        InsectKind.RARE -> rare
        InsectKind.GOLDEN -> golden
    }.normalized()

    fun withLook(kind: InsectKind, look: KindLook): BeetleSkinSet {
        val fixed = look.copy(model = BeetleModel.fromName(look.model.name, kind)).normalized()
        return when (kind) {
            InsectKind.NORMAL -> copy(normal = fixed)
            InsectKind.FAST -> copy(fast = fixed)
            InsectKind.RARE -> copy(rare = fixed)
            InsectKind.GOLDEN -> copy(golden = fixed)
        }
    }

    fun normalized(): BeetleSkinSet = copy(
        name = name.trim(),
        normal = normal.copy(model = BeetleModel.fromName(normal.model.name, InsectKind.NORMAL)).normalized(),
        fast = fast.copy(model = BeetleModel.fromName(fast.model.name, InsectKind.FAST)).normalized(),
        rare = rare.copy(model = BeetleModel.fromName(rare.model.name, InsectKind.RARE)).normalized(),
        golden = golden.copy(model = BeetleModel.fromName(golden.model.name, InsectKind.GOLDEN)).normalized()
    )

    companion object {
        const val MIN_SIZE_SCALE = 0.7f
        const val MAX_SIZE_SCALE = 1.6f
        const val SIZE_STEPS = 18
        private const val SIZE_STEP = 0.05f
        const val DEFAULT_ID = "default"

        val DEFAULT = BeetleSkinSet(
            id = DEFAULT_ID,
            name = "Классический",
            builtIn = true,
            normal = BeetleModel.BEETLE_CLASSIC.defaultLook(),
            fast = BeetleModel.ROACH_CLASSIC.defaultLook(),
            rare = BeetleModel.LADY_CLASSIC.defaultLook(),
            golden = BeetleModel.GOLD_CLASSIC.defaultLook()
        )

        fun builtIns(): List<BeetleSkinSet> = listOf(
            DEFAULT,
            BeetleSkinSet(
                id = "forest",
                name = "Лесная стража",
                builtIn = true,
                normal = BeetleModel.BEETLE_RHINO.defaultLook().copy(
                    bodyColor = 0xFF33691E.toInt(),
                    accentColor = 0xFF1B5E20.toInt(),
                    mood = BeetleMood.KIND
                ),
                fast = BeetleModel.ROACH_ARMOR.defaultLook().copy(
                    bodyColor = 0xFF5D4037.toInt(),
                    accentColor = 0xFF8D6E63.toInt()
                ),
                rare = BeetleModel.LADY_HEART.defaultLook(),
                golden = BeetleModel.GOLD_SCARAB.defaultLook()
            ),
            BeetleSkinSet(
                id = "nightmare",
                name = "Ночной кошмар",
                builtIn = true,
                normal = BeetleModel.BEETLE_STAG.defaultLook().copy(
                    bodyColor = 0xFF212121.toInt(),
                    accentColor = 0xFFB71C1C.toInt(),
                    eyeColor = 0xFFFF1744.toInt(),
                    sizeScale = 1.15f,
                    mood = BeetleMood.EVIL
                ),
                fast = BeetleModel.ROACH_RACER.defaultLook().copy(
                    bodyColor = 0xFF000000.toInt(),
                    accentColor = 0xFFD50000.toInt(),
                    eyeColor = 0xFFFF5252.toInt(),
                    mood = BeetleMood.EVIL
                ),
                rare = BeetleModel.LADY_ROYAL.defaultLook().copy(
                    bodyColor = 0xFF4A148C.toInt(),
                    accentColor = 0xFF000000.toInt(),
                    eyeColor = 0xFFFF8A80.toInt(),
                    mood = BeetleMood.EVIL
                ),
                golden = BeetleModel.GOLD_JEWEL.defaultLook().copy(
                    bodyColor = 0xFFBF360C.toInt(),
                    accentColor = 0xFFFFD600.toInt(),
                    eyeColor = 0xFFFF1744.toInt(),
                    mood = BeetleMood.EVIL
                )
            )
        )

        fun clampSize(value: Float): Float = value.coerceIn(MIN_SIZE_SCALE, MAX_SIZE_SCALE)

        fun sizeProgress(sizeScale: Float): Int {
            val clamped = clampSize(sizeScale)
            return ((clamped - MIN_SIZE_SCALE) / SIZE_STEP).toInt().coerceIn(0, SIZE_STEPS)
        }

        fun sizeFromProgress(progress: Int): Float {
            return clampSize(MIN_SIZE_SCALE + progress.coerceIn(0, SIZE_STEPS) * SIZE_STEP)
        }

        fun scaledSize(baseSize: Float, look: KindLook): Float =
            baseSize * clampSize(look.sizeScale)
    }
}
