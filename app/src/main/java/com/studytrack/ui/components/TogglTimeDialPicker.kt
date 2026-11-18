package com.studytrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.*
import kotlin.math.*

/**
 * Toggl Track-style circular time dial.
 *
 * One full 360-degree rotation corresponds to exactly 1 hour (60 minutes).
 * Dragging clockwise past 12 o'clock increments the hour.
 * Dragging counter-clockwise past 12 o'clock decrements the hour.
 */
@Composable
fun TogglTimeDial(
    timeMillis: Long,
    onTimeChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "START TIME",
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val haptic = LocalHapticFeedback.current

    val calendar = remember(timeMillis) {
        Calendar.getInstance().apply { this.timeInMillis = timeMillis }
    }

    var hour by remember(timeMillis) { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember(timeMillis) { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var previousAngle by remember { mutableFloatStateOf(minute * 6f) }

    fun updateTimestamp(newHour: Int, newMinute: Int) {
        val cal = Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, newHour)
            set(Calendar.MINUTE, newMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        onTimeChange(cal.timeInMillis)
    }

    fun handleAngleChange(newAngle: Float) {
        // Detect crossing 12 o'clock (0° / 360°)
        if (previousAngle > 270f && newAngle < 90f) {
            // Clockwise crossing past 12: increment hour
            hour = (hour + 1) % 24
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else if (previousAngle < 90f && newAngle > 270f) {
            // Counter-clockwise crossing past 12: decrement hour
            hour = (hour - 1 + 24) % 24
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        previousAngle = newAngle
        val newMin = ((newAngle / 6f).roundToInt()) % 60
        if (newMin != minute) {
            minute = newMin
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            updateTimestamp(hour, minute)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time digital readout with +/- 1h quick adjustment buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    hour = (hour - 1 + 24) % 24
                    updateTimestamp(hour, minute)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "-1 hour",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "%02d:%02d".format(hour, minute),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = {
                    hour = (hour + 1) % 24
                    updateTimestamp(hour, minute)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "+1 hour",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "1 giro completo = 1 ora (+/-)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Circular Dial Canvas
        val dialSize = 240.dp
        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

        Box(
            modifier = Modifier
                .size(dialSize)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                            if (angle < 0f) angle += 360f
                            previousAngle = angle
                            handleAngleChange(angle)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                            if (angle < 0f) angle += 360f
                            handleAngleChange(angle)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                        if (angle < 0f) angle += 360f
                        previousAngle = angle
                        handleAngleChange(angle)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val trackRadius = (size.minDimension / 2f) - 24.dp.toPx()

                // Background track circle
                drawCircle(
                    color = surfaceColor,
                    radius = trackRadius,
                    center = center,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // 60-minute tick marks around track
                for (i in 0 until 60) {
                    val tickAngleRad = Math.toRadians((i * 6 - 90).toDouble())
                    val isMajor = (i % 5 == 0)
                    val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                    val tickStroke = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()
                    val tickColor = if (isMajor) onSurfaceColor else onSurfaceVariant

                    val startRadius = trackRadius - (tickLen / 2f)
                    val endRadius = trackRadius + (tickLen / 2f)

                    val startX = center.x + startRadius * cos(tickAngleRad).toFloat()
                    val startY = center.y + startRadius * sin(tickAngleRad).toFloat()
                    val endX = center.x + endRadius * cos(tickAngleRad).toFloat()
                    val endY = center.y + endRadius * sin(tickAngleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = tickStroke,
                        cap = StrokeCap.Round
                    )
                }

                // Active progress arc from top (0 min) to current minute
                val sweepAngle = minute * 6f
                if (sweepAngle > 0f) {
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                        size = androidx.compose.ui.geometry.Size(trackRadius * 2f, trackRadius * 2f),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draggable Handle / Knob
                val currentAngleRad = Math.toRadians((minute * 6 - 90).toDouble())
                val handleCenter = Offset(
                    center.x + trackRadius * cos(currentAngleRad).toFloat(),
                    center.y + trackRadius * sin(currentAngleRad).toFloat()
                )

                // Outer handle glow / circle
                drawCircle(
                    color = primaryColor,
                    radius = 16.dp.toPx(),
                    center = handleCenter
                )
                // Inner white dot
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = handleCenter
                )
            }
        }
    }
}

/**
 * Modal dialog for adjusting start or end time with the Toggl circular dial.
 */
@Composable
fun TogglTimeDialDialog(
    initialTimeMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Modifica Orario",
    label: String = "START TIME"
) {
    var selectedTime by remember { mutableLongStateOf(initialTimeMillis) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                TogglTimeDial(
                    timeMillis = selectedTime,
                    onTimeChange = { selectedTime = it },
                    label = label
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annulla")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedTime) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Conferma", color = Color.White)
                    }
                }
            }
        }
    }
}
