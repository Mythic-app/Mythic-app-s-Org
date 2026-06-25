package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin

@Composable
fun NeonBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_animation")
    
    val bubble1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bubble1_alpha"
    )
    
    val bubble2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bubble2_alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Bubble 1
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF9AF04D).copy(alpha = bubble1Alpha), Color.Transparent)),
            radius = width * 0.4f,
            center = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.2f)
        )

        // Bubble 2
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF4D9AF0).copy(alpha = bubble2Alpha), Color.Transparent)),
            radius = width * 0.5f,
            center = androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.7f)
        )
    }
}
