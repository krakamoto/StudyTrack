package com.studytrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeEntryCard(
    description: String,
    projectName: String?,
    projectColorHex: String?,
    startTime: Long,
    endTime: Long?,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null
) {
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startStr = timeFmt.format(Date(startTime))
    val endStr = if (endTime != null) timeFmt.format(Date(endTime)) else "Running"
    val durationMillis = (endTime ?: System.currentTimeMillis()) - startTime
    val durationStr = formatDuration(durationMillis)

    val projectColor = projectColorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Project color dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(projectColor ?: MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Description + project name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = description.ifBlank { "(No description)" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (projectName != null) {
                    Text(
                        text = projectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = projectColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Times + duration
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$startStr – $endStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = durationStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            if (onEdit != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%dh %02dm".format(hours, minutes)
    else if (minutes > 0) "%dm %02ds".format(minutes, seconds)
    else "%ds".format(seconds)
}
