package com.example.teme.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun FloatingIndicator(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(key1 = text) {
        launch {
            offsetY.animateTo(
                targetValue = -100f,
                animationSpec = tween(durationMillis = 1500)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1500, delayMillis = 500)
            )
        }
    }

    Text(
        text = text,
        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.toInt()) }
            .alpha(alpha.value)
    )
}
