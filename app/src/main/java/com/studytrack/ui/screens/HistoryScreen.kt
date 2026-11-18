package com.studytrack.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studytrack.ui.components.TimeEntryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToEdit: (Long) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("History", fontWeight = FontWeight.Bold)
                },
                actions = {
                    // Filter by project
                    ExposedDropdownMenuBox(
                        expanded = state.showFilterDropdown,
                        onExpandedChange = { viewModel.toggleFilterDropdown() }
                    ) {
                        IconButton(
                            onClick = viewModel::toggleFilterDropdown,
                            modifier = Modifier.menuAnchor()
                        ) {
                            Icon(
                                Icons.Filled.FilterList,
                                "Filter",
                                tint = if (state.filterProjectId != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = state.showFilterDropdown,
                            onDismissRequest = viewModel::toggleFilterDropdown
                        ) {
                            DropdownMenuItem(
                                text = { Text("All projects") },
                                onClick = { viewModel.setFilter(null) }
                            )
                            state.projects.forEach { p ->
                                val pColor = try {
                                    Color(android.graphics.Color.parseColor(p.colorHex))
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
                                            Spacer(Modifier.width(8.dp))
                                            Text(p.name)
                                        }
                                    },
                                    onClick = { viewModel.setFilter(p.id) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (state.groupedEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No entries yet.\nStart tracking to see your history.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                state.groupedEntries.forEach { (dateLabel, entries) ->
                    item(key = "header-$dateLabel") {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    items(entries, key = { it.id }) { entry ->
                        val project = state.projectFor(entry.projectId)
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteEntry(entry)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.surface,
                                    label = "swipeBg"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            TimeEntryCard(
                                description = entry.description,
                                projectName = project?.name,
                                projectColorHex = project?.colorHex,
                                startTime = entry.startTime,
                                endTime = entry.endTime,
                                isRunning = entry.isRunning,
                                onEdit = { onNavigateToEdit(entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
