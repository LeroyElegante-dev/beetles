package com.example.beetles.game

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.beetles.model.BeetleSkinSet

class BeetleSetPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var skinSet: BeetleSkinSet = BeetleSkinSet.DEFAULT
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        val kinds = InsectKind.entries
        val cell = width / kinds.size.toFloat()
        val size = minOf(cell, height.toFloat()) * 0.88f
        kinds.forEachIndexed { index, kind ->
            val look = skinSet.look(kind)
            val x = index * cell + (cell - size) / 2f
            val y = (height - size) / 2f
            BeetleSkinRenderer.draw(canvas, x, y, size, look)
        }
    }
}
