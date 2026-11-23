package com.example.beetles.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.beetles.model.BeetleModel
import com.example.beetles.model.BeetleMood
import com.example.beetles.model.KindLook

object BeetleSkinRenderer {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun draw(canvas: Canvas, x: Float, y: Float, size: Float, look: KindLook) {
        if (size <= 1f) return
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(size / 64f, size / 64f)
        when (look.model) {
            BeetleModel.BEETLE_CLASSIC -> drawBeetleClassic(canvas, look)
            BeetleModel.BEETLE_RHINO -> drawBeetleRhino(canvas, look)
            BeetleModel.BEETLE_STAG -> drawBeetleStag(canvas, look)
            BeetleModel.ROACH_CLASSIC -> drawRoachClassic(canvas, look)
            BeetleModel.ROACH_RACER -> drawRoachRacer(canvas, look)
            BeetleModel.ROACH_ARMOR -> drawRoachArmor(canvas, look)
            BeetleModel.LADY_CLASSIC -> drawLadyClassic(canvas, look)
            BeetleModel.LADY_HEART -> drawLadyHeart(canvas, look)
            BeetleModel.LADY_ROYAL -> drawLadyRoyal(canvas, look)
            BeetleModel.GOLD_CLASSIC -> drawGoldClassic(canvas, look)
            BeetleModel.GOLD_SCARAB -> drawGoldScarab(canvas, look)
            BeetleModel.GOLD_JEWEL -> drawGoldJewel(canvas, look)
        }
        drawFace(canvas, look)
        canvas.restore()
    }

    private fun drawBeetleClassic(canvas: Canvas, look: KindLook) {
        fill(look.accentColor)
        canvas.drawPath(leg(18f, 34f, 8f, 26f, 10f, 22f, 20f, 32f), fillPaint)
        canvas.drawPath(leg(46f, 34f, 56f, 26f, 54f, 22f, 44f, 32f), fillPaint)
        canvas.drawPath(leg(18f, 44f, 6f, 50f, 8f, 54f, 20f, 46f), fillPaint)
        canvas.drawPath(leg(46f, 44f, 58f, 50f, 56f, 54f, 44f, 46f), fillPaint)
        oval(canvas, 16f, 14f, 48f, 54f, look.bodyColor)
        oval(canvas, 22f, 10f, 42f, 30f, look.accentColor)
    }

    private fun drawBeetleRhino(canvas: Canvas, look: KindLook) {
        fill(look.accentColor)
        canvas.drawPath(leg(18f, 36f, 7f, 28f, 9f, 24f, 20f, 34f), fillPaint)
        canvas.drawPath(leg(46f, 36f, 57f, 28f, 55f, 24f, 44f, 34f), fillPaint)
        canvas.drawPath(leg(18f, 46f, 6f, 54f, 9f, 58f, 21f, 48f), fillPaint)
        canvas.drawPath(leg(46f, 46f, 58f, 54f, 55f, 58f, 43f, 48f), fillPaint)
        oval(canvas, 16f, 18f, 48f, 56f, look.bodyColor)
        oval(canvas, 22f, 14f, 42f, 34f, darker(look.bodyColor))
        fill(look.accentColor)
        val horn = Path().apply {
            moveTo(32f, 4f)
            lineTo(36f, 20f)
            lineTo(28f, 20f)
            close()
        }
        canvas.drawPath(horn, fillPaint)
        oval(canvas, 29f, 16f, 35f, 24f, look.accentColor)
    }

    private fun drawBeetleStag(canvas: Canvas, look: KindLook) {
        fill(look.accentColor)
        val leftPincer = Path().apply {
            moveTo(24f, 18f)
            quadTo(10f, 4f, 8f, 16f)
            quadTo(12f, 14f, 22f, 22f)
            close()
        }
        val rightPincer = Path().apply {
            moveTo(40f, 18f)
            quadTo(54f, 4f, 56f, 16f)
            quadTo(52f, 14f, 42f, 22f)
            close()
        }
        canvas.drawPath(leftPincer, fillPaint)
        canvas.drawPath(rightPincer, fillPaint)
        canvas.drawPath(leg(18f, 38f, 6f, 32f, 8f, 28f, 20f, 36f), fillPaint)
        canvas.drawPath(leg(46f, 38f, 58f, 32f, 56f, 28f, 44f, 36f), fillPaint)
        canvas.drawPath(leg(18f, 48f, 6f, 56f, 9f, 60f, 21f, 50f), fillPaint)
        canvas.drawPath(leg(46f, 48f, 58f, 56f, 55f, 60f, 43f, 50f), fillPaint)
        oval(canvas, 16f, 20f, 48f, 56f, look.bodyColor)
        oval(canvas, 22f, 16f, 42f, 32f, darker(look.bodyColor))
    }

    private fun drawRoachClassic(canvas: Canvas, look: KindLook) {
        fill(look.accentColor)
        canvas.drawPath(leg(20f, 22f, 6f, 12f, 10f, 8f, 24f, 20f), fillPaint)
        canvas.drawPath(leg(44f, 22f, 58f, 12f, 54f, 8f, 40f, 20f), fillPaint)
        canvas.drawPath(leg(18f, 34f, 4f, 34f, 4f, 40f, 18f, 40f), fillPaint)
        canvas.drawPath(leg(46f, 34f, 60f, 34f, 60f, 40f, 46f, 40f), fillPaint)
        canvas.drawPath(leg(20f, 46f, 8f, 56f, 12f, 60f, 24f, 48f), fillPaint)
        canvas.drawPath(leg(44f, 46f, 56f, 56f, 52f, 60f, 40f, 48f), fillPaint)
        oval(canvas, 20f, 8f, 44f, 56f, look.bodyColor)
        oval(canvas, 24f, 10f, 40f, 26f, look.accentColor)
        stroke(darker(look.bodyColor), 1.6f)
        canvas.drawLine(32f, 18f, 32f, 50f, strokePaint)
    }

    private fun drawRoachRacer(canvas: Canvas, look: KindLook) {
        fill(look.accentColor)
        canvas.drawPath(leg(22f, 20f, 8f, 6f, 12f, 4f, 24f, 16f), fillPaint)
        canvas.drawPath(leg(42f, 20f, 56f, 6f, 52f, 4f, 40f, 16f), fillPaint)
        canvas.drawPath(leg(20f, 32f, 4f, 28f, 4f, 34f, 20f, 36f), fillPaint)
        canvas.drawPath(leg(44f, 32f, 60f, 28f, 60f, 34f, 44f, 36f), fillPaint)
        canvas.drawPath(leg(22f, 48f, 10f, 60f, 14f, 62f, 24f, 50f), fillPaint)
        canvas.drawPath(leg(42f, 48f, 54f, 60f, 50f, 62f, 40f, 50f), fillPaint)
        oval(canvas, 24f, 6f, 40f, 58f, look.bodyColor)
        fill(look.accentColor)
        canvas.drawRoundRect(RectF(27f, 22f, 37f, 52f), 4f, 4f, fillPaint)
        stroke(look.accentColor, 2.4f)
        canvas.drawLine(28f, 8f, 18f, 0f, strokePaint)
        canvas.drawLine(36f, 8f, 46f, 0f, strokePaint)
    }

    private fun drawRoachArmor(canvas: Canvas, look: KindLook) {
        fill(look.bodyColor)
        canvas.drawPath(leg(20f, 24f, 6f, 16f, 9f, 12f, 22f, 22f), fillPaint)
        canvas.drawPath(leg(44f, 24f, 58f, 16f, 55f, 12f, 42f, 22f), fillPaint)
        canvas.drawPath(leg(18f, 36f, 4f, 36f, 4f, 42f, 18f, 42f), fillPaint)
        canvas.drawPath(leg(46f, 36f, 60f, 36f, 60f, 42f, 46f, 42f), fillPaint)
        canvas.drawPath(leg(20f, 48f, 8f, 58f, 12f, 62f, 24f, 50f), fillPaint)
        canvas.drawPath(leg(44f, 48f, 56f, 58f, 52f, 62f, 40f, 50f), fillPaint)
        oval(canvas, 20f, 8f, 44f, 56f, look.bodyColor)
        fill(look.accentColor)
        canvas.drawRoundRect(RectF(22f, 14f, 42f, 22f), 4f, 4f, fillPaint)
        canvas.drawRoundRect(RectF(22f, 26f, 42f, 34f), 4f, 4f, fillPaint)
        canvas.drawRoundRect(RectF(22f, 38f, 42f, 46f), 4f, 4f, fillPaint)
        oval(canvas, 24f, 8f, 40f, 20f, darker(look.bodyColor))
    }

    private fun drawLadyClassic(canvas: Canvas, look: KindLook) {
        oval(canvas, 12f, 12f, 52f, 56f, look.bodyColor)
        oval(canvas, 22f, 8f, 42f, 28f, look.accentColor)
        fill(look.accentColor)
        canvas.drawCircle(22f, 34f, 5f, fillPaint)
        canvas.drawCircle(42f, 34f, 5f, fillPaint)
        canvas.drawCircle(28f, 46f, 4.5f, fillPaint)
        canvas.drawCircle(38f, 46f, 4.5f, fillPaint)
        stroke(look.accentColor, 2f)
        canvas.drawLine(32f, 24f, 32f, 54f, strokePaint)
    }

    private fun drawLadyHeart(canvas: Canvas, look: KindLook) {
        fill(look.bodyColor)
        val heart = Path().apply {
            moveTo(32f, 56f)
            cubicTo(8f, 38f, 10f, 16f, 32f, 24f)
            cubicTo(54f, 16f, 56f, 38f, 32f, 56f)
            close()
        }
        canvas.drawPath(heart, fillPaint)
        oval(canvas, 22f, 10f, 42f, 28f, look.accentColor)
        fill(look.accentColor)
        val spot = Path().apply {
            moveTo(32f, 44f)
            cubicTo(24f, 36f, 24f, 30f, 32f, 34f)
            cubicTo(40f, 30f, 40f, 36f, 32f, 44f)
            close()
        }
        canvas.drawPath(spot, fillPaint)
        canvas.drawCircle(22f, 32f, 4f, fillPaint)
        canvas.drawCircle(42f, 32f, 4f, fillPaint)
    }

    private fun drawLadyRoyal(canvas: Canvas, look: KindLook) {
        oval(canvas, 12f, 14f, 52f, 58f, look.bodyColor)
        oval(canvas, 22f, 8f, 42f, 26f, look.accentColor)
        fill(look.accentColor)
        canvas.drawCircle(20f, 32f, 4f, fillPaint)
        canvas.drawCircle(32f, 30f, 4f, fillPaint)
        canvas.drawCircle(44f, 32f, 4f, fillPaint)
        canvas.drawCircle(24f, 44f, 4f, fillPaint)
        canvas.drawCircle(40f, 44f, 4f, fillPaint)
        canvas.drawCircle(32f, 50f, 3.5f, fillPaint)
        val crown = Path().apply {
            moveTo(22f, 12f)
            lineTo(26f, 4f)
            lineTo(32f, 12f)
            lineTo(38f, 4f)
            lineTo(42f, 12f)
            close()
        }
        canvas.drawPath(crown, fillPaint)
        fill(look.bodyColor)
        canvas.drawCircle(26f, 6f, 2f, fillPaint)
        canvas.drawCircle(32f, 4f, 2.2f, fillPaint)
        canvas.drawCircle(38f, 6f, 2f, fillPaint)
    }

    private fun drawGoldClassic(canvas: Canvas, look: KindLook) {
        drawRoachClassic(canvas, look)
        fill(look.accentColor)
        canvas.drawRoundRect(RectF(24f, 28f, 40f, 40f), 3f, 3f, fillPaint)
    }

    private fun drawGoldScarab(canvas: Canvas, look: KindLook) {
        fill(look.accentColor)
        canvas.drawPath(leg(18f, 34f, 6f, 24f, 9f, 20f, 20f, 32f), fillPaint)
        canvas.drawPath(leg(46f, 34f, 58f, 24f, 55f, 20f, 44f, 32f), fillPaint)
        canvas.drawPath(leg(18f, 46f, 6f, 56f, 10f, 60f, 22f, 48f), fillPaint)
        canvas.drawPath(leg(46f, 46f, 58f, 56f, 54f, 60f, 42f, 48f), fillPaint)
        oval(canvas, 14f, 16f, 50f, 56f, look.bodyColor)
        fill(look.accentColor)
        canvas.drawCircle(32f, 10f, 8f, fillPaint)
        oval(canvas, 24f, 6f, 40f, 16f, look.accentColor)
        stroke(darker(look.accentColor), 2f)
        canvas.drawLine(32f, 22f, 32f, 50f, strokePaint)
        canvas.drawLine(20f, 34f, 44f, 34f, strokePaint)
    }

    private fun drawGoldJewel(canvas: Canvas, look: KindLook) {
        fill(look.bodyColor)
        canvas.drawPath(leg(18f, 36f, 6f, 28f, 8f, 24f, 20f, 34f), fillPaint)
        canvas.drawPath(leg(46f, 36f, 58f, 28f, 56f, 24f, 44f, 34f), fillPaint)
        canvas.drawPath(leg(18f, 48f, 6f, 56f, 10f, 60f, 22f, 50f), fillPaint)
        canvas.drawPath(leg(46f, 48f, 58f, 56f, 54f, 60f, 42f, 50f), fillPaint)
        oval(canvas, 16f, 14f, 48f, 56f, look.bodyColor)
        oval(canvas, 22f, 10f, 42f, 28f, darker(look.bodyColor))
        val gem = Path().apply {
            moveTo(32f, 24f)
            lineTo(42f, 36f)
            lineTo(32f, 48f)
            lineTo(22f, 36f)
            close()
        }
        fill(look.accentColor)
        canvas.drawPath(gem, fillPaint)
        fill(lighten(look.accentColor))
        canvas.drawCircle(32f, 34f, 4f, fillPaint)
    }

    private fun drawFace(canvas: Canvas, look: KindLook) {
        if (look.mood == BeetleMood.EVIL) {
            stroke(Color.BLACK, 2.6f)
            canvas.drawLine(22f, 16f, 30f, 12f, strokePaint)
            canvas.drawLine(42f, 16f, 34f, 12f, strokePaint)
            fill(look.eyeColor)
            canvas.drawCircle(26f, 18f, 3.4f, fillPaint)
            canvas.drawCircle(38f, 18f, 3.4f, fillPaint)
            fill(Color.BLACK)
            canvas.drawCircle(26.6f, 18.4f, 1.5f, fillPaint)
            canvas.drawCircle(38.6f, 18.4f, 1.5f, fillPaint)
            stroke(Color.BLACK, 2f)
            canvas.drawLine(28f, 26f, 32f, 24f, strokePaint)
            canvas.drawLine(36f, 26f, 32f, 24f, strokePaint)
        } else {
            fill(look.eyeColor)
            canvas.drawCircle(26f, 18f, 3.2f, fillPaint)
            canvas.drawCircle(38f, 18f, 3.2f, fillPaint)
            fill(Color.BLACK)
            canvas.drawCircle(26.4f, 18.4f, 1.4f, fillPaint)
            canvas.drawCircle(38.4f, 18.4f, 1.4f, fillPaint)
            stroke(Color.BLACK, 2f)
            canvas.drawArc(RectF(26f, 22f, 38f, 32f), 20f, 140f, false, strokePaint)
        }
    }

    private fun oval(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, color: Int) {
        fill(color)
        canvas.drawOval(RectF(l, t, r, b), fillPaint)
    }

    private fun leg(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float): Path {
        return Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
            lineTo(x3, y3)
            lineTo(x4, y4)
            close()
        }
    }

    private fun fill(color: Int) {
        fillPaint.color = color
        fillPaint.style = Paint.Style.FILL
    }

    private fun stroke(color: Int, width: Float) {
        strokePaint.color = color
        strokePaint.strokeWidth = width
    }

    private fun darker(color: Int): Int {
        return Color.argb(
            255,
            (Color.red(color) * 0.65f).toInt().coerceIn(0, 255),
            (Color.green(color) * 0.65f).toInt().coerceIn(0, 255),
            (Color.blue(color) * 0.65f).toInt().coerceIn(0, 255)
        )
    }

    private fun lighten(color: Int): Int {
        return Color.argb(
            255,
            (Color.red(color) + (255 - Color.red(color)) * 0.45f).toInt().coerceIn(0, 255),
            (Color.green(color) + (255 - Color.green(color)) * 0.45f).toInt().coerceIn(0, 255),
            (Color.blue(color) + (255 - Color.blue(color)) * 0.45f).toInt().coerceIn(0, 255)
        )
    }
}
