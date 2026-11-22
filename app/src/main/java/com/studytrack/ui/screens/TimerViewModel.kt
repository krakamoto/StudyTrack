package com.studytrack.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity
import com.studytrack.data.repository.StudyTrackRepository
import com.studytrack.domain.usecases.StartTimerUseCase
import com.studytrack.domain.usecases.StopTimerUseCase
import com.studytrack.notifications.TimerNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerUiState(
    val isRunning: Boolean = false,
    val description: String = "",
    val selectedProjectId: Long? = null,
    val elapsedMillis: Long = 0L,
    val currentEntryId: Long? = null,
    val runningStartTime: Long = 0L,
    val showAdjustRunningStartTime: Boolean = false,
    val recentEntries: List<TimeEntryEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val showProjectDropdown: Boolean = false,
    val entryToEdit: TimeEntryEntity? = null,
    val isSyncing: Boolean = false
) {
    val elapsedDisplay: String
        get() {
            val totalSeconds = elapsedMillis / 1000
            val h = totalSeconds / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60
            return "%02d:%02d:%02d".format(h, m, s)
        }

    fun projectFor(id: Long?) = projects.firstOrNull { it.id == id }
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: StudyTrackRepository,
    private val startTimerUseCase: StartTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null
    private var runningStartTime: Long = 0L
    private var metadataSaveJob: Job? = null

    init {
        observeRunningEntry()
        observeProjects()
        observeRecentEntries()
    }

    private fun observeRunningEntry() {
        viewModelScope.launch {
            repository.getCurrentRunningEntry().collect { entry ->
                if (entry != null) {
                    updateTimerNotification(running = true)
                    runningStartTime = entry.startTime
                    _uiState.update {
                        it.copy(
                            isRunning = true,
                            description = entry.description,
                            selectedProjectId = entry.projectId,
                            currentEntryId = entry.id,
                            runningStartTime = entry.startTime,
                            elapsedMillis = (System.currentTimeMillis() - entry.startTime).coerceAtLeast(0L)
                        )
                    }
                    startTicking()
                } else {
                    updateTimerNotification(running = false)
                    stopTicking()
                    _uiState.update {
                        it.copy(
                            isRunning = false,
                            elapsedMillis = 0L,
                            currentEntryId = null,
                            runningStartTime = 0L,
                            showAdjustRunningStartTime = false
                        )
                    }
                }
            }
        }
    }

    private fun observeProjects() {
        viewModelScope.launch {
            repository.getAllProjects().collect { list ->
                _uiState.update { it.copy(projects = list) }
            }
        }
    }

    private fun observeRecentEntries() {
        val todayStart = run {
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }
        viewModelScope.launch {
            repository.getEntriesByDateRange(todayStart, Long.MAX_VALUE).collect { entries ->
                _uiState.update { state ->
                    state.copy(recentEntries = entries.filter { !it.isRunning }.take(5))
                }
            }
        }
    }

    private fun updateTimerNotification(running: Boolean) {
        // Starting/stopping a foreground service can be rejected by the OS in
        // unusual background states; the timer must keep working regardless.
        runCatching {
            if (running) {
                TimerNotificationService.start(appContext)
            } else {
                TimerNotificationService.stop(appContext)
            }
        }
    }

    private fun startTicking() {
        if (tickJob?.isActive == true) return
        tickJob = viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(elapsedMillis = System.currentTimeMillis() - runningStartTime) }
                delay(1000L)
            }
        }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
        scheduleRunningMetadataSave()
    }

    fun onProjectSelected(id: Long?) {
        _uiState.update { it.copy(selectedProjectId = id, showProjectDropdown = false) }
        scheduleRunningMetadataSave()
    }

    /**
     * The description and project of a running session stay editable. Writes
     * are debounced so typing doesn't cause a database write per character.
     */
    private fun scheduleRunningMetadataSave() {
        val state = _uiState.value
        if (!state.isRunning || state.currentEntryId == null) return
        metadataSaveJob?.cancel()
        metadataSaveJob = viewModelScope.launch {
            delay(350L)
            persistRunningMetadata()
        }
    }

    private suspend fun persistRunningMetadata() {
        val state = _uiState.value
        val entryId = state.currentEntryId ?: return
        val entry = repository.getTimeEntryById(entryId) ?: return
        val description = state.description.trim()
        if (entry.description == description && entry.projectId == state.selectedProjectId) return
        repository.updateTimeEntry(
            entry.copy(
                description = description,
                projectId = state.selectedProjectId
            )
        )
    }

    fun toggleProjectDropdown() {
        _uiState.update { it.copy(showProjectDropdown = !it.showProjectDropdown) }
    }

    fun start() {
        val state = _uiState.value
        if (state.isRunning || state.isSyncing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                startTimerUseCase(
                    description = state.description.trim(),
                    projectId = state.selectedProjectId
                )
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                // Flush pending edits before the timer is closed.
                metadataSaveJob?.cancel()
                persistRunningMetadata()
                stopTimerUseCase()
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun openAdjustRunningStartTime() {
        if (_uiState.value.isRunning && _uiState.value.currentEntryId != null) {
            _uiState.update { it.copy(showAdjustRunningStartTime = true) }
        }
    }

    fun closeAdjustRunningStartTime() {
        _uiState.update { it.copy(showAdjustRunningStartTime = false) }
    }

    fun updateRunningStartTime(newStartTimeMillis: Long) {
        val entryId = _uiState.value.currentEntryId ?: return
        // A session cannot start in the future. Clamp to now instead of
        // silently displaying a negative or frozen timer.
        val safeStartTime = newStartTimeMillis.coerceAtMost(System.currentTimeMillis())
        runningStartTime = safeStartTime
        val newElapsed = (System.currentTimeMillis() - safeStartTime).coerceAtLeast(0L)
        _uiState.update {
            it.copy(
                runningStartTime = safeStartTime,
                elapsedMillis = newElapsed,
                showAdjustRunningStartTime = false
            )
        }
        viewModelScope.launch {
            val entry = repository.getTimeEntryById(entryId)
            if (entry != null) {
                repository.updateTimeEntry(entry.copy(startTime = safeStartTime))
            }
        }
    }

    fun saveEntryEdits(
        entry: TimeEntryEntity,
        description: String,
        projectId: Long?,
        startTime: Long,
        endTime: Long?
    ) {
        viewModelScope.launch {
            repository.updateTimeEntry(
                entry.copy(
                    description = description.trim(),
                    projectId = projectId,
                    startTime = startTime,
                    endTime = endTime,
                    isRunning = endTime == null
                )
            )
            _uiState.update { it.copy(entryToEdit = null) }
        }
    }

    fun deleteEntry(entry: TimeEntryEntity) {
        viewModelScope.launch {
            repository.deleteTimeEntry(entry.id)
            _uiState.update { it.copy(entryToEdit = null) }
        }
    }

    fun openEdit(entry: TimeEntryEntity) {
        _uiState.update { it.copy(entryToEdit = entry) }
    }

    fun closeEdit() {
        _uiState.update { it.copy(entryToEdit = null) }
    }
}
