package com.artmondo.boxters.particles

import androidx.compose.ui.graphics.Color
import com.artmondo.boxters.ui.theme.GameColors
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 1.0f,
    var decay: Float = 0.01f,
    var size: Float = 2f,
    var color: Color = GameColors.uiAccent,
    var alpha: Float = 1.0f,
    var gravity: Float = 0f,
    var friction: Float = 0.99f
) {
    fun update(dt: Float) {
        vy += gravity * dt
        vx *= friction
        vy *= friction
        x += vx * dt * 60f
        y += vy * dt * 60f
        life -= decay * dt * 60f
        alpha = max(0f, life)
    }

    val isDead: Boolean get() = life <= 0f
}

data class BackgroundStar(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float,
    val speed: Float,
    val phase: Float,
    val color: Color
)

class ParticleSystem {
    val particles = mutableListOf<Particle>()
    val stars: List<BackgroundStar>

    init {
        stars = List(150) {
            BackgroundStar(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 1.5f + 0.5f,
                brightness = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.2f,
                phase = Random.nextFloat() * PI.toFloat() * 2f,
                color = GameColors.starColors[Random.nextInt(GameColors.starColors.size)]
            )
        }
    }

    fun emit(x: Float, y: Float, count: Int, config: ParticleConfig = ParticleConfig()) {
        for (i in 0 until count) {
            val angle = (PI.toFloat() * 2f * i) / count + Random.nextFloat() * 0.5f
            val speed = config.speed * (0.5f + Random.nextFloat() * 0.5f)
            particles.add(Particle(
                x = x, y = y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                life = config.life,
                decay = config.decay,
                size = config.size.let { if (it <= 0f) Random.nextFloat() * 3f + 1f else it },
                color = config.color,
                gravity = config.gravity,
                friction = config.friction
            ))
        }
    }

    fun emitVictoryBurst(x: Float, y: Float) {
        val colors = listOf(GameColors.uiAccent, GameColors.uiSuccess, Color(0xFF60A5FA), GameColors.uiWarning, Color(0xFFC084FC))
        for (i in 0 until 60) {
            val angle = (PI.toFloat() * 2f * i) / 60f
            val speed = 3f + Random.nextFloat() * 4f
            val color = colors[Random.nextInt(colors.size)]
            particles.add(Particle(
                x = x, y = y,
                vx = cos(angle) * speed, vy = sin(angle) * speed,
                life = 1.0f, decay = 0.008f,
                size = Random.nextFloat() * 4f + 2f, color = color,
                gravity = 0.02f, friction = 0.97f
            ))
        }
    }

    fun emitClearEffect(x: Float, y: Float) {
        emit(x, y, 10, ParticleConfig(
            speed = 2.5f, life = 0.7f, decay = 0.02f,
            size = 3f, color = GameColors.clearAccent, friction = 0.95f
        ))
    }

    fun emitChainReplace(x: Float, y: Float) {
        emit(x, y, 8, ParticleConfig(
            speed = 1.5f, life = 0.6f, decay = 0.025f,
            size = 2.5f, color = GameColors.chainAccent, friction = 0.97f
        ))
    }

    fun emitIlluminate(x: Float, y: Float) {
        emit(x, y, 6, ParticleConfig(
            speed = 1.0f, life = 1.0f, decay = 0.012f,
            size = 3f, color = GameColors.illuminateAccent, friction = 0.99f
        ))
    }

    fun emitConfetti(width: Float, height: Float) {
        val colors = listOf(
            GameColors.uiAccent, GameColors.boardHighlight, GameColors.uiSuccess,
            Color(0xFF60A5FA), Color(0xFFC084FC), GameColors.uiWarning,
            Color(0xFFEC4899), Color(0xFF22D3EE)
        )
        for (i in 0 until 60) {
            val x = Random.nextFloat() * width
            particles.add(Particle(
                x = x, y = -10f - Random.nextFloat() * 200f,
                vx = (Random.nextFloat() - 0.5f) * 2f,
                vy = 1.5f + Random.nextFloat() * 2.5f,
                life = 1.0f, decay = 0.008f,
                size = Random.nextFloat() * 4f + 2f,
                color = colors[Random.nextInt(colors.size)],
                gravity = 0.03f, friction = 0.995f
            ))
        }
    }

    fun update(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.update(dt)
            if (p.isDead) iter.remove()
        }
    }

    fun clear() {
        particles.clear()
    }
}

data class ParticleConfig(
    val speed: Float = 2f,
    val life: Float = 1.0f,
    val decay: Float = 0.02f,
    val size: Float = 0f,
    val color: Color = GameColors.uiAccent,
    val gravity: Float = 0f,
    val friction: Float = 0.98f
)
