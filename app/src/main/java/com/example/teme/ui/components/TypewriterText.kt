package com.example.teme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    typingSpeedMs: Long = 50L
) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        for (i in text.indices) {
            displayedText += text[i]
            delay(typingSpeedMs)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFDE7)) // light pale yellow
            .border(4.dp, Color(0xFF212121))
            .padding(4.dp)
            .border(2.dp, Color(0xFF212121))
            .padding(12.dp)
    ) {
        Text(
            text = displayedText,
            color = Color(0xFF212121),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
