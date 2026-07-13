package com.example.teme.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun ConfettiParticleSystem(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    // Generate random particles once
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat(),
                yOffset = Random.nextFloat(),
                speed = Random.nextFloat() * 2f + 1f,
                color = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta).random(),
                size = Random.nextFloat() * 15f + 5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val y = ((progress * particle.speed + particle.yOffset) % 1f) * size.height
            val x = particle.x * size.width + (kotlin.math.sin(progress * Math.PI * 4 * particle.speed) * 20f).toFloat()
            
            drawRect(
                color = particle.color,
                topLeft = Offset(x, y),
                size = Size(particle.size, particle.size)
            )
        }
    }
}

data class ConfettiParticle(
    val x: Float, // 0 to 1 relative width
    val yOffset: Float, // 0 to 1 relative height start
    val speed: Float,
    val color: Color,
    val size: Float
)
