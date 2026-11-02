package com.example.beetles.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.beetles.R
import com.example.beetles.model.GameSettings
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SensorEventListener {

    var onHit: ((points: Int) -> Unit)? = null
    var onMiss: (() -> Unit)? = null
    var onBonusCollected: (() -> Unit)? = null

    private val insects = mutableListOf<Insect>()
    private val random = Random.Default
    private val kindDrawables = InsectKind.entries.associateWith { kind ->
        ContextCompat.getDrawable(context, kind.drawableRes)!!.mutate()
    }
    private val bonusDrawable: Drawable =
        ContextCompat.getDrawable(context, R.drawable.ic_bonus)!!.mutate()

    private var settings = GameSettings.DEFAULT
    private var running = false
    private var lastFrameNs = 0L
    private var timeSinceSpawn = 0f
    private var timeSinceBonus = 0f
    private var bonus: Bonus? = null
    private var tiltSecondsLeft = 0f
    private var gravityX = 0f
    private var gravityY = 0f
    private var sensorRegistered = false

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val screamSoundId: Int = soundPool.load(context, R.raw.beetle_scream, 1)

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
        bonus = null
        timeSinceSpawn = 0f
        timeSinceBonus = 0f
        tiltSecondsLeft = 0f
        gravityX = 0f
        gravityY = 0f
        unregisterTiltSensor()
        running = true
        lastFrameNs = System.nanoTime()
        removeCallbacks(frameLoop)
        postOnAnimation(frameLoop)
        invalidate()
    }

    fun stop() {
        running = false
        removeCallbacks(frameLoop)
        tiltSecondsLeft = 0f
        unregisterTiltSensor()
    }

    override fun onDetachedFromWindow() {
        stop()
        soundPool.release()
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
        bonus?.let { item ->
            bonusDrawable.setBounds(
                item.x.toInt(),
                item.y.toInt(),
                (item.x + item.size).toInt(),
                (item.y + item.size).toInt()
            )
            bonusDrawable.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!running || event.action != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event)
        }
        performClick()
        val currentBonus = bonus
        if (currentBonus != null && currentBonus.contains(event.x, event.y)) {
            collectBonus()
            invalidate()
            return true
        }
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

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        // Portrait: left edge down → insects slide left; top edge down → slide up.
        gravityX = -event.values[0]
        gravityY = event.values[1]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun update(dt: Float) {
        if (width == 0 || height == 0) return
        val speedScale = settings.speed / 5f
        val tiltActive = tiltSecondsLeft > 0f
        if (tiltActive) {
            tiltSecondsLeft = (tiltSecondsLeft - dt).coerceAtLeast(0f)
            if (tiltSecondsLeft == 0f) {
                unregisterTiltSensor()
            }
        }

        insects.forEach { insect ->
            if (tiltActive) {
                insect.vx += gravityX * TILT_ACCEL * dt
                insect.vy += gravityY * TILT_ACCEL * dt
                insect.vx *= TILT_DAMPING
                insect.vy *= TILT_DAMPING
            }
            insect.x += insect.vx * dt * speedScale
            insect.y += insect.vy * dt * speedScale
            bounce(insect, stickToEdges = tiltActive)
        }

        timeSinceSpawn += dt
        val spawnEvery = (0.9f / speedScale).coerceIn(0.35f, 1.2f)
        if (timeSinceSpawn >= spawnEvery && insects.size < settings.maxBeetles) {
            spawnInsect()
            timeSinceSpawn = 0f
        }

        if (bonus == null) {
            timeSinceBonus += dt
            if (timeSinceBonus >= settings.bonusIntervalSeconds) {
                spawnBonus()
                timeSinceBonus = 0f
            }
        }
    }

    private fun bounce(insect: Insect, stickToEdges: Boolean) {
        val maxX = (width - insect.size).coerceAtLeast(0f)
        val maxY = (height - insect.size).coerceAtLeast(0f)
        if (insect.x < 0f) {
            insect.x = 0f
            insect.vx = if (stickToEdges) insect.vx.coerceAtLeast(0f) else -insect.vx
        } else if (insect.x > maxX) {
            insect.x = maxX
            insect.vx = if (stickToEdges) insect.vx.coerceAtMost(0f) else -insect.vx
        }
        if (insect.y < 0f) {
            insect.y = 0f
            insect.vy = if (stickToEdges) insect.vy.coerceAtLeast(0f) else -insect.vy
        } else if (insect.y > maxY) {
            insect.y = maxY
            insect.vy = if (stickToEdges) insect.vy.coerceAtMost(0f) else -insect.vy
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

    private fun spawnBonus() {
        val size = (minOf(width, height) * 0.12f).coerceIn(64f, 120f)
        val maxX = (width - size).coerceAtLeast(1f)
        val maxY = (height - size).coerceAtLeast(1f)
        bonus = Bonus(
            x = random.nextFloat() * maxX,
            y = random.nextFloat() * maxY,
            size = size
        )
    }

    private fun collectBonus() {
        bonus = null
        timeSinceBonus = 0f
        tiltSecondsLeft = TILT_DURATION_SECONDS
        registerTiltSensor()
        soundPool.play(screamSoundId, 1f, 1f, 1, 0, 1f)
        onBonusCollected?.invoke()
    }

    private fun registerTiltSensor() {
        val sensor = accelerometer ?: return
        if (sensorRegistered) return
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        sensorRegistered = true
    }

    private fun unregisterTiltSensor() {
        if (!sensorRegistered) return
        sensorManager.unregisterListener(this)
        sensorRegistered = false
        gravityX = 0f
        gravityY = 0f
    }

    private data class Bonus(
        var x: Float,
        var y: Float,
        val size: Float
    ) {
        fun contains(touchX: Float, touchY: Float): Boolean {
            return touchX >= x && touchX <= x + size && touchY >= y && touchY <= y + size
        }
    }

    private companion object {
        const val TILT_DURATION_SECONDS = 10f
        const val TILT_ACCEL = 220f
        const val TILT_DAMPING = 0.985f
    }
}
