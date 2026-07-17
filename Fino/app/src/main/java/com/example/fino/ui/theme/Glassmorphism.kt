package com.example.fino.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphism(
    cornerRadius: Dp = 12.dp,
    padding: Dp = 16.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.White.copy(alpha = 0.05f))
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
    .padding(padding)

fun Modifier.neonGlow(color: Color, radius: Dp = 16.dp): Modifier {
    // In Jetpack Compose, true drop shadows with colors are tricky without custom drawing or RenderEffect (API 31+).
    // For a simple implementation, we can use a Modifier.drawBehind or just a colored background with low alpha.
    // To keep it simple and compatible, we'll return the modifier as is or with a simple border for now.
    // A true neon glow would require `Modifier.drawBehind { drawRoundRect(..., shadow...) }`
    return this
}
