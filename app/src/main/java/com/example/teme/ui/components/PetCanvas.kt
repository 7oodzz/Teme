package com.example.teme.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.teme.domain.model.PetState
import com.example.teme.domain.model.RoomItem

@Composable
fun PetCanvas(
    modifier: Modifier = Modifier,
    petState: PetState,
    unlockedItems: List<RoomItem>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet_animation")
    
    // Bounce animation for IDLE
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (petState == PetState.IDLE) 15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Eye blink animation
    val blink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3800
                0f at 3900 // Blink shut
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Zzz animation for ASLEEP
    val zzzOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (petState == PetState.ASLEEP) -50f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "zzz"
    )

    val zzzAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "zzz_alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2 + 50f
        
        // Draw Room Items
        unlockedItems.forEach { item ->
            when(item.name) {
                "Tiny Cactus" -> {
                    drawRect(Color(0xFF81C784), topLeft = Offset(cx - 150f, cy - 60f), size = Size(30f, 60f))
                    drawRect(Color(0xFF5D4037), topLeft = Offset(cx - 160f, cy), size = Size(50f, 30f))
                }
                "Cozy Rug" -> {
                    drawOval(Color(0xFFFFCC80).copy(alpha = 0.5f), topLeft = Offset(cx - 150f, cy + 80f), size = Size(300f, 60f))
                }
                "Desk Lamp" -> {
                    drawRect(Color(0xFFFDD835), topLeft = Offset(cx + 100f, cy - 80f), size = Size(40f, 40f))
                    drawRect(Color(0xFF757575), topLeft = Offset(cx + 115f, cy - 40f), size = Size(10f, 60f))
                    drawRect(Color(0xFF424242), topLeft = Offset(cx + 100f, cy + 20f), size = Size(40f, 10f))
                }
                "Retro Poster" -> {
                    drawRect(Color(0xFF64B5F6), topLeft = Offset(cx - 80f, cy - 250f), size = Size(160f, 100f))
                    drawRect(Color(0xFF1565C0), topLeft = Offset(cx - 80f, cy - 250f), size = Size(160f, 100f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
                }
            }
        }

        // Pet Base (Frog/Cat shape)
        val petColor = Color(0xFFAED581) // Pastel Green
        val yOffset = bounceOffset
        
        // Body
        drawRoundRect(
            color = petColor,
            topLeft = Offset(cx - 60f, cy - 80f + yOffset),
            size = Size(120f, 100f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
        )

        // Eyes
        val eyeHeight = 15f * blink
        if (petState == PetState.ASLEEP) {
            // Closed eyes line
            drawLine(Color.Black, Offset(cx - 30f, cy - 50f + yOffset), Offset(cx - 10f, cy - 50f + yOffset), strokeWidth = 3f)
            drawLine(Color.Black, Offset(cx + 10f, cy - 50f + yOffset), Offset(cx + 30f, cy - 50f + yOffset), strokeWidth = 3f)
            
            // Zzz
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 40f
                alpha = (zzzAlpha * 255).toInt()
            }
            drawContext.canvas.nativeCanvas.drawText("Z", cx + 40f, cy - 100f + yOffset + zzzOffset, paint)
            paint.textSize = 30f
            drawContext.canvas.nativeCanvas.drawText("z", cx + 60f, cy - 120f + yOffset + (zzzOffset*1.5f), paint)
        } else {
            // Open eyes
            drawOval(Color.Black, topLeft = Offset(cx - 30f, cy - 60f + yOffset + (15f - eyeHeight)/2), size = Size(15f, eyeHeight))
            drawOval(Color.Black, topLeft = Offset(cx + 15f, cy - 60f + yOffset + (15f - eyeHeight)/2), size = Size(15f, eyeHeight))
        }

        // Focusing (Typing at desk)
        if (petState == PetState.FOCUSING) {
            drawRect(Color(0xFF795548), topLeft = Offset(cx - 80f, cy - 10f + yOffset), size = Size(160f, 40f)) // Desk
            drawRect(Color(0xFF9E9E9E), topLeft = Offset(cx - 30f, cy - 30f + yOffset), size = Size(60f, 40f)) // Laptop
            // Typing hands
            val typingBounce = (bounceOffset % 5)
            drawCircle(Color(0xFFAED581), radius = 10f, center = Offset(cx - 20f, cy - 10f + yOffset + typingBounce))
            drawCircle(Color(0xFFAED581), radius = 10f, center = Offset(cx + 20f, cy - 10f + yOffset - typingBounce))
        }
    }
}
