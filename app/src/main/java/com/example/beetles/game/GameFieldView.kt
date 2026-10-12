package com.example.beetles.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.beetles.model.GameSettings
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onHit: ((points: Int) -> Unit)? = null
    var onMiss: (() -> Unit)? = null

    private val insects = mutableListOf<Insect>()
    private val random = Random.Default
    private val kindDrawables = InsectKind.entries.associateWith { kind ->
        ContextCompat.getDrawable(context, kind.drawableRes)!!.mutate()
    }

    private var settings = GameSettings.DEFAULT
    private var running = false
    private var lastFrameNs = 0L
    private var timeSinceSpawn = 0f

    private val frameLoop = object : Runnable {
        override fun run() {
            if (!running) return
            val now = System.nanoTime()
            val dt = ((now - lastFrameNs) / 1_000_000_000f).coerceIn(0f, 0.05f)
            lastFrameNs = now
            update(dt)
            invalidate()
            postOnAnimation(this)
        }
    }

    fun start(settings: GameSettings) {
        this.settings = settings
        insects.clear()
        timeSinceSpawn = 0f
        running = true
        lastFrameNs = System.nanoTime()
        removeCallbacks(frameLoop)
        postOnAnimation(frameLoop)
        invalidate()
    }

    fun stop() {
        running = false
        removeCallbacks(frameLoop)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        insects.forEach { insect ->
            val drawable: Drawable = kindDrawables.getValue(insect.kind)
            drawable.setBounds(
                insect.x.toInt(),
                insect.y.toInt(),
                (insect.x + insect.size).toInt(),
                (insect.y + insect.size).toInt()
            )
            drawable.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!running || event.action != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event)
        }
        performClick()
        val hit = insects.lastOrNull { it.contains(event.x, event.y) }
        if (hit != null) {
            insects.remove(hit)
            onHit?.invoke(hit.kind.points)
            invalidate()
        } else {
            onMiss?.invoke()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun update(dt: Float) {
        if (width == 0 || height == 0) return
        val speedScale = settings.speed / 5f
        insects.forEach { insect ->
            insect.x += insect.vx * dt * speedScale
            insect.y += insect.vy * dt * speedScale
            bounce(insect)
        }
        timeSinceSpawn += dt
        val spawnEvery = (0.9f / speedScale).coerceIn(0.35f, 1.2f)
        if (timeSinceSpawn >= spawnEvery && insects.size < settings.maxBeetles) {
            spawnInsect()
            timeSinceSpawn = 0f
        }
    }

    private fun bounce(insect: Insect) {
        val maxX = (width - insect.size).coerceAtLeast(0f)
        val maxY = (height - insect.size).coerceAtLeast(0f)
        if (insect.x < 0f) {
            insect.x = 0f
            insect.vx = -insect.vx
        } else if (insect.x > maxX) {
            insect.x = maxX
            insect.vx = -insect.vx
        }
        if (insect.y < 0f) {
            insect.y = 0f
            insect.vy = -insect.vy
        } else if (insect.y > maxY) {
            insect.y = maxY
            insect.vy = -insect.vy
        }
    }

    private fun spawnInsect() {
        val kind = InsectKind.random(random)
        val size = ((72 + random.nextInt(32)) * 3.5f * kind.sizeMultiplier)
            .coerceAtMost(minOf(width, height) * 0.8f)
        val maxX = (width - size).coerceAtLeast(1f)
        val maxY = (height - size).coerceAtLeast(1f)
        val angle = random.nextFloat() * (Math.PI * 2).toFloat()
        val baseSpeed = (140f + random.nextFloat() * 120f) * kind.speedMultiplier
        insects += Insect(
            x = random.nextFloat() * maxX,
            y = random.nextFloat() * maxY,
            vx = cos(angle) * baseSpeed,
            vy = sin(angle) * baseSpeed,
            size = size,
            kind = kind
        )
    }
}
