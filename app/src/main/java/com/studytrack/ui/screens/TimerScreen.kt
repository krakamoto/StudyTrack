package com.studytrack.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studytrack.ui.components.TimeEntryCard
import com.studytrack.ui.components.TimerDisplay
import com.studytrack.ui.components.formatDuration
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    onNavigateToEdit: (Long) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Pulsing scale animation for running indicator ring
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    // Show edit dialog if entry is selected
    state.entryToEdit?.let { entry ->
        EditTimeEntryDialog(
            entry = entry,
            projects = state.projects,
            onDismiss = { viewModel.closeEdit() }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Timer hero section ────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing ring + timer display
                Box(contentAlignment = Alignment.Center) {
                    if (state.isRunning) {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .scale(ringScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        TimerDisplay(
                            timeText = state.elapsedDisplay,
                            isRunning = state.isRunning,
                            fontSize = 36.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Description field
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("What are you studying?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isRunning,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Project selector
                ExposedDropdownMenuBox(
                    expanded = state.showProjectDropdown,
                    onExpandedChange = { if (!state.isRunning) viewModel.toggleProjectDropdown() }
                ) {
                    OutlinedTextField(
                        value = state.projectFor(state.selectedProjectId)?.name ?: "No project",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Project") },
                        leadingIcon = {
                            Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        },
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !state.isRunning
                    )
                    ExposedDropdownMenu(
                        expanded = state.showProjectDropdown,
                        onDismissRequest = viewModel::toggleProjectDropdown
                    ) {
                        DropdownMenuItem(
                            text = { Text("No project") },
                            onClick = { viewModel.onProjectSelected(null) }
                        )
                        state.projects.forEach { project ->
                            val pColor = try {
                                Color(android.graphics.Color.parseColor(project.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(pColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(project.name)
                                    }
                                },
                                onClick = { viewModel.onProjectSelected(project.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Start / Stop FAB
                FloatingActionButton(
                    onClick = { if (state.isRunning) viewModel.stop() else viewModel.start() },
                    containerColor = if (state.isRunning) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (state.isRunning) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isRunning) "Stop" else "Start",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (state.recentEntries.isNotEmpty()) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // ── Recent entries ────────────────────────────────────────────────
        items(state.recentEntries, key = { it.id }) { entry ->
            val project = state.projectFor(entry.projectId)
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .combinedClickable(
                        onClick = { onNavigateToEdit(entry.id) },
                        onLongClick = { viewModel.openEdit(entry) }
                    )
            ) {
                TimeEntryCard(
                    description = entry.description,
                    projectName = project?.name,
                    projectColorHex = project?.colorHex,
                    startTime = entry.startTime,
                    endTime = entry.endTime,
                    isRunning = entry.isRunning
                )
            }
        }
    }
}
