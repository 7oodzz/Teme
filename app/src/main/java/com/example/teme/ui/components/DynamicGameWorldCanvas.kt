package com.example.teme.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import com.example.teme.domain.model.PetState
import com.example.teme.domain.model.RoomItem
import java.util.Calendar
import kotlin.random.Random

@Composable
fun DynamicGameWorldCanvas(
    modifier: Modifier = Modifier,
    petState: PetState,
    unlockedItems: List<RoomItem>,
    timerProgress: Float,
    onPetTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "world_animation")
    
    // Time of day gradient logic
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val (bgColorTop, bgColorBottom, isNight) = when (hour) {
        in 6..11 -> Triple(Color(0xFFFFCC80), Color(0xFFFFF9C4), false) // Morning
        in 12..16 -> Triple(Color(0xFF81D4FA), Color(0xFFE1F5FE), false) // Afternoon
        in 17..19 -> Triple(Color(0xFFCE93D8), Color(0xFFFFCC80), false) // Twilight
        else -> Triple(Color(0xFF1A237E), Color(0xFF311B92), true) // Night
    }

    // Fire animation
    val fireFlicker by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(150 + Random.nextInt(100), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire_flicker"
    )

    // Pet bounce/squash
    var isSquashing by remember { mutableStateOf(false) }
    val squashFactor by animateFloatAsState(
        targetValue = if (isSquashing) 0.7f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = { isSquashing = false },
        label = "squash"
    )

    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (petState == PetState.IDLE) 15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Particles
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )
    
    val particles = remember { List(20) { Offset(Random.nextFloat(), Random.nextFloat()) } }

    Canvas(modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                // Simple hit box for the pet
                val cx = size.width / 2
                val cy = size.height / 2 + 50f
                val petRect = androidx.compose.ui.geometry.Rect(
                    left = cx - 60f - 100f, // Offset because pet is moved left of campfire
                    top = cy - 80f,
                    right = cx + 60f - 100f,
                    bottom = cy + 20f
                )
                if (petRect.contains(offset)) {
                    isSquashing = true
                    onPetTap()
                }
            }
        }
    ) {
        val cx = size.width / 2
        val cy = size.height / 2 + 50f

        // Draw Background
        drawRect(
            brush = Brush.verticalGradient(listOf(bgColorTop, bgColorBottom)),
            size = size
        )

        // Draw Particles
        val particleColor = if (isNight) Color(0xFFAEEA00).copy(alpha = 0.6f) else Color(0xFFFFD54F).copy(alpha = 0.4f)
        particles.forEach { p ->
            val pX = (p.x * size.width + (particleProgress * 100f)) % size.width
            val pY = (p.y * size.height - (particleProgress * 200f))
            val finalPy = if (pY < 0) pY + size.height else pY
            drawCircle(color = particleColor, radius = 4f, center = Offset(pX, finalPy))
        }

        // Filter for active items only
        val activeItems = unlockedItems.filter { it.isActive }

        // Draw active Room Items
        activeItems.forEach { item ->
            when(item.name) {
                "Tiny Cactus" -> {
                    drawRect(Color(0xFF81C784), topLeft = Offset(cx - 200f, cy - 60f), size = Size(30f, 60f))
                    drawRect(Color(0xFF5D4037), topLeft = Offset(cx - 210f, cy), size = Size(50f, 30f))
                }
                "Cozy Rug" -> {
                    drawOval(Color(0xFFFFCC80).copy(alpha = 0.5f), topLeft = Offset(cx - 200f, cy + 80f), size = Size(400f, 60f))
                }
                "Desk Lamp" -> {
                    drawRect(Color(0xFFFDD835), topLeft = Offset(cx - 20f, cy - 100f), size = Size(40f, 40f))
                    drawRect(Color(0xFF757575), topLeft = Offset(cx - 5f, cy - 60f), size = Size(10f, 60f))
                }
                "Retro Poster" -> {
                    drawRect(Color(0xFF64B5F6), topLeft = Offset(cx - 150f, cy - 250f), size = Size(160f, 100f))
                    drawRect(Color(0xFF1565C0), topLeft = Offset(cx - 150f, cy - 250f), size = Size(160f, 100f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
                }
                "Retro PC" -> {
                    // Monitor
                    drawRect(Color(0xFFE0E0E0), topLeft = Offset(cx + 120f, cy - 100f), size = Size(80f, 60f))
                    drawRect(Color(0xFF212121), topLeft = Offset(cx + 125f, cy - 95f), size = Size(70f, 50f))
                    // Keyboard
                    drawRect(Color(0xFF9E9E9E), topLeft = Offset(cx + 110f, cy - 30f), size = Size(100f, 20f))
                }
                "Game Console" -> {
                    // Gray console box under TV or by fire
                    drawRect(Color(0xFF9E9E9E), topLeft = Offset(cx + 60f, cy + 50f), size = Size(60f, 20f))
                    drawRect(Color(0xFF424242), topLeft = Offset(cx + 70f, cy + 55f), size = Size(40f, 5f))
                }
                "Picture Frame" -> {
                    // Frame on wall
                    drawRect(Color(0xFF8D6E63), topLeft = Offset(cx + 100f, cy - 220f), size = Size(60f, 80f))
                    drawRect(Color(0xFFFFF9C4), topLeft = Offset(cx + 105f, cy - 215f), size = Size(50f, 70f))
                    // Simple drawing inside
                    drawCircle(Color(0xFFFF8A65), radius = 10f, center = Offset(cx + 130f, cy - 180f))
                }
                "Action Figure" -> {
                    // Small figure
                    drawRect(Color(0xFFEF5350), topLeft = Offset(cx - 150f, cy - 10f), size = Size(20f, 30f))
                    drawCircle(Color(0xFFFFCC80), radius = 8f, center = Offset(cx - 140f, cy - 15f))
                }
            }
        }

        // --- Draw Pet ---
        val petCx = cx - 100f // Move pet left
        val petColor = Color(0xFFAED581)
        val yOffset = bounceOffset

        // Squash logic
        val petHeight = 100f * squashFactor
        val petTop = cy - 80f + yOffset + (100f - petHeight)

        drawRoundRect(
            color = petColor,
            topLeft = Offset(petCx - 60f, petTop),
            size = Size(120f, petHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
        )

        // Eyes
        if (petState == PetState.ASLEEP) {
            drawLine(Color.Black, Offset(petCx - 30f, petTop + 30f), Offset(petCx - 10f, petTop + 30f), strokeWidth = 3f)
            drawLine(Color.Black, Offset(petCx + 10f, petTop + 30f), Offset(petCx + 30f, petTop + 30f), strokeWidth = 3f)
        } else {
            drawOval(Color.Black, topLeft = Offset(petCx - 30f, petTop + 20f), size = Size(15f, 15f))
            drawOval(Color.Black, topLeft = Offset(petCx + 15f, petTop + 20f), size = Size(15f, 15f))
        }

        // --- Draw Campfire (Timer) ---
        val fireCx = cx + 80f
        val fireCy = cy + 40f
        
        // Logs
        drawRoundRect(Color(0xFF5D4037), topLeft = Offset(fireCx - 40f, fireCy), size = Size(80f, 20f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
        drawRoundRect(Color(0xFF4E342E), topLeft = Offset(fireCx - 30f, fireCy - 10f), size = Size(60f, 20f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))

        if (petState == PetState.ASLEEP) {
            // Ashes
            drawCircle(Color.Gray, radius = 20f, center = Offset(fireCx, fireCy - 10f))
            drawCircle(Color.DarkGray, radius = 15f, center = Offset(fireCx - 10f, fireCy - 15f))
        } else {
            // Active Fire
            val maxFireHeight = 120f
            val currentFireHeight = (maxFireHeight * timerProgress).coerceAtLeast(20f) * fireFlicker
            
            val firePath = Path().apply {
                moveTo(fireCx - 30f, fireCy)
                quadraticBezierTo(fireCx - 40f, fireCy - currentFireHeight/2, fireCx, fireCy - currentFireHeight)
                quadraticBezierTo(fireCx + 40f, fireCy - currentFireHeight/2, fireCx + 30f, fireCy)
                close()
            }
            
            drawPath(
                path = firePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFEB3B), Color(0xFFFF9800), Color(0xFFF44336)),
                    startY = fireCy - currentFireHeight,
                    endY = fireCy
                )
            )

            // Embers
            if (timerProgress < 1f) {
                val emberProgress = (particleProgress * 3) % 1f
                drawCircle(Color(0xFFFF9800), radius = 3f, center = Offset(fireCx + 10f, fireCy - currentFireHeight - (emberProgress * 50f)))
                drawCircle(Color(0xFFFFEB3B), radius = 2f, center = Offset(fireCx - 15f, fireCy - currentFireHeight - ((emberProgress + 0.5f)%1f * 60f)))
            }
        }
    }
}
