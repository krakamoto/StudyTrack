package com.studytrack.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity
import com.studytrack.ui.components.TimePickerDialog
import com.studytrack.ui.components.TogglTimeDialDialog
import java.text.SimpleDateFormat
import java.util.*

// ── Full-screen edit (routed via nav) ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimeEntryScreen(
    viewModel: EditTimeEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDone) {
        if (state.isDone) onNavigateBack()
    }

    EditTimeEntryContent(
        state = state,
        onDescriptionChange = viewModel::onDescriptionChange,
        onProjectSelected = viewModel::onProjectSelected,
        onStartTimeChange = viewModel::onStartTimeChange,
        onEndTimeChange = viewModel::onEndTimeChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onBack = onNavigateBack
    )
}

// ── Inline dialog (opened from long-press on timer screen) ──────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimeEntryDialog(
    entry: TimeEntryEntity,
    projects: List<ProjectEntity>,
    onDismiss: () -> Unit
) {
    // Local state for the dialog
    var description by remember { mutableStateOf(entry.description) }
    var selectedProjectId by remember { mutableStateOf(entry.projectId) }
    var startTime by remember { mutableLongStateOf(entry.startTime) }
    var endTime by remember { mutableStateOf(entry.endTime) }
    var showProjectDropdown by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    fun startCal() = Calendar.getInstance().apply { timeInMillis = startTime }
    fun endCal() = Calendar.getInstance().apply { timeInMillis = endTime ?: startTime }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Project dropdown
                ExposedDropdownMenuBox(
                    expanded = showProjectDropdown,
                    onExpandedChange = { showProjectDropdown = it }
                ) {
                    val proj = projects.firstOrNull { it.id == selectedProjectId }
                    OutlinedTextField(
                        value = proj?.name ?: "No project",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Project") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showProjectDropdown,
                        onDismissRequest = { showProjectDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No project") },
                            onClick = { selectedProjectId = null; showProjectDropdown = false }
                        )
                        projects.forEach { p ->
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
                                onClick = { selectedProjectId = p.id; showProjectDropdown = false }
                            )
                        }
                    }
                }

                // Time row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start: ${timeFmt.format(Date(startTime))}")
                    }
                    if (endTime != null) {
                        OutlinedButton(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("End: ${timeFmt.format(Date(endTime!!))}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {}
    )

    if (showStartTimePicker) {
        TogglTimeDialDialog(
            initialTimeMillis = startTime,
            onConfirm = {
                startTime = it
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
            title = "Modifica Ora Inizio",
            label = "START TIME"
        )
    }

    if (showEndTimePicker && endTime != null) {
        TogglTimeDialDialog(
            initialTimeMillis = endTime!!,
            onConfirm = {
                endTime = it
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
            title = "Modifica Ora Fine",
            label = "END TIME"
        )
    }
}

// ── Shared content used by both Screen + Dialog (screen mode) ───────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTimeEntryContent(
    state: EditTimeEntryUiState,
    onDescriptionChange: (String) -> Unit,
    onProjectSelected: (Long?) -> Unit,
    onStartTimeChange: (Long) -> Unit,
    onEndTimeChange: (Long) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var showProjectDropdown by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    fun startCal() = Calendar.getInstance().apply { timeInMillis = state.startTime }
    fun endCal() = Calendar.getInstance().apply { timeInMillis = state.endTime ?: state.startTime }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Project selector
            ExposedDropdownMenuBox(
                expanded = showProjectDropdown,
                onExpandedChange = { showProjectDropdown = it }
            ) {
                val proj = state.projects.firstOrNull { it.id == state.selectedProjectId }
                OutlinedTextField(
                    value = proj?.name ?: "No project",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Project") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showProjectDropdown,
                    onDismissRequest = { showProjectDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("No project") },
                        onClick = { onProjectSelected(null); showProjectDropdown = false }
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
                            onClick = { onProjectSelected(p.id); showProjectDropdown = false }
                        )
                    }
                }
            }

            // Date
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date: ${dateFmt.format(Date(state.startTime))}")
            }

            // Time row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start\n${timeFmt.format(Date(state.startTime))}")
                }
                OutlinedButton(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.weight(1f),
                    enabled = state.endTime != null
                ) {
                    Text("End\n${state.endTime?.let { timeFmt.format(Date(it)) } ?: "Running"}")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Save Changes")
            }
        }
    }

    // Toggl Circular Time pickers
    if (showStartTimePicker) {
        TogglTimeDialDialog(
            initialTimeMillis = state.startTime,
            onConfirm = {
                onStartTimeChange(it)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
            title = "Modifica Ora Inizio",
            label = "START TIME"
        )
    }

    if (showEndTimePicker && state.endTime != null) {
        TogglTimeDialDialog(
            initialTimeMillis = state.endTime!!,
            onConfirm = {
                onEndTimeChange(it)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
            title = "Modifica Ora Fine",
            label = "END TIME"
        )
    }

    // Date picker
    if (showDatePicker) {
        val cal = startCal()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val updated = cal.apply {
                    set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                }.timeInMillis
                onStartTimeChange(updated)
                // Shift endTime by same delta if present
                if (state.endTime != null) {
                    val delta = updated - state.startTime
                    onEndTimeChange(state.endTime + delta)
                }
                showDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply { setOnDismissListener { showDatePicker = false } }.show()
        showDatePicker = false // prevent re-show
    }

    // Delete confirm
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
