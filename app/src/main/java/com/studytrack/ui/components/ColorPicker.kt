package com.studytrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val PROJECT_COLORS = listOf(
    "#E50914", // Red (brand)
    "#7B61FF", // Purple
    "#00BCD4", // Cyan
    "#4CAF50", // Green
    "#FF9800", // Orange
    "#F06292", // Pink
    "#42A5F5", // Blue
    "#FFEB3B", // Yellow
    "#26A69A", // Teal
    "#AB47BC"  // Violet
)

@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = PROJECT_COLORS.chunked(5)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowColors ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowColors.forEach { hex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    val isSelected = hex == selectedColor
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                ) else Modifier
                            )
                            .clickable { onColorSelected(hex) }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
