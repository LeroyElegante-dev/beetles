package com.example.beetles.game

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.beetles.model.BeetleModel
import com.example.beetles.model.BeetleSkinSet
import com.example.beetles.model.KindLook

class BeetlePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var look: KindLook = BeetleModel.BEETLE_CLASSIC.defaultLook()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val base = minOf(width, height) * 0.86f
        val size = BeetleSkinSet.scaledSize(base, look)
        val x = (width - size) / 2f
        val y = (height - size) / 2f
        BeetleSkinRenderer.draw(canvas, x, y, size, look)
    }
}
