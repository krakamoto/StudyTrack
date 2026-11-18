package com.studytrack.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TimerDisplay(
    timeText: String,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 64.sp,
    color: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 0.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerAlpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onBackground else color,
            modifier = if (isRunning) Modifier.alpha(alpha) else Modifier
        )
    }
}
