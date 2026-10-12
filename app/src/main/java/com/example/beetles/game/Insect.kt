package com.example.beetles.game

data class Insect(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val kind: InsectKind
) {
    fun contains(touchX: Float, touchY: Float): Boolean {
        return touchX >= x && touchX <= x + size && touchY >= y && touchY <= y + size
    }
}
