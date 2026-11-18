package com.studytrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProjectChip(
    projectName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val chipColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    if (onClick != null) {
        FilterChip(
            selected = false,
            onClick = onClick,
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(chipColor)
                    )
                    Text(text = projectName, style = MaterialTheme.typography.bodySmall)
                }
            },
            modifier = modifier,
            shape = RoundedCornerShape(50)
        )
    } else {
        Surface(
            shape = RoundedCornerShape(50),
            color = chipColor.copy(alpha = 0.18f),
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(chipColor)
                )
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = chipColor
                )
            }
        }
    }
}
