package com.example.teme.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RetroStatBar(
    modifier: Modifier = Modifier,
    progress: Float,
    fillColor: Color,
    backgroundColor: Color = Color(0xFFE0E0E0),
    borderColor: Color = Color(0xFF212121)
) {
    Canvas(modifier = modifier.fillMaxWidth().height(24.dp)) {
        val barWidth = size.width
        val barHeight = size.height
        val fillWidth = barWidth * progress.coerceIn(0f, 1f)
        
        // Shadow/Border block
        drawRect(
            color = borderColor,
            topLeft = Offset(4f, 4f),
            size = Size(barWidth, barHeight)
        )
        
        // Background track
        drawRect(
            color = backgroundColor,
            topLeft = Offset(0f, 0f),
            size = Size(barWidth, barHeight)
        )
        
        // Fill area
        if (fillWidth > 0) {
            drawRect(
                color = fillColor,
                topLeft = Offset(0f, 0f),
                size = Size(fillWidth, barHeight)
            )
            
            // Retro internal stripes
            val stripeColor = Color.White.copy(alpha = 0.3f)
            val stripeWidth = 10f
            val spacing = 20f
            var startX = 0f
            
            while (startX < fillWidth) {
                // Diagonal line drawing
                val x1 = startX
                val y1 = barHeight
                val x2 = startX + barHeight
                val y2 = 0f
                
                // Need to clip the stripes to the fillWidth manually or just draw short lines
                if (x1 < fillWidth) {
                    val endX = minOf(x2, fillWidth)
                    val endY = if (x2 > fillWidth) barHeight - (fillWidth - x1) else 0f
                    
                    drawLine(
                        color = stripeColor,
                        start = Offset(x1, y1),
                        end = Offset(endX, endY),
                        strokeWidth = stripeWidth
                    )
                }
                startX += spacing
            }
        }
        
        // Outer thick border
        drawRect(
            color = borderColor,
            topLeft = Offset(0f, 0f),
            size = Size(barWidth, barHeight),
            style = Stroke(width = 4f)
        )
    }
}
