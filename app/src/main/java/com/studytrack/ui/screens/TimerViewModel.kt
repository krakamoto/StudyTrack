package com.studytrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity
import com.studytrack.data.repository.StudyTrackRepository
import com.studytrack.domain.usecases.StartTimerUseCase
import com.studytrack.domain.usecases.StopTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val recentEntries: List<TimeEntryEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val showProjectDropdown: Boolean = false,
    val entryToEdit: TimeEntryEntity? = null
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
    private val repository: StudyTrackRepository,
    private val startTimerUseCase: StartTimerUseCase,
    private val stopTimerUseCase: StopTimerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null
    private var runningStartTime: Long = 0L

    init {
        observeRunningEntry()
        observeProjects()
        observeRecentEntries()
    }

    private fun observeRunningEntry() {
        viewModelScope.launch {
            repository.getCurrentRunningEntry().collect { entry ->
                if (entry != null) {
                    runningStartTime = entry.startTime
                    _uiState.update {
                        it.copy(
                            isRunning = true,
                            description = entry.description,
                            selectedProjectId = entry.projectId,
                            currentEntryId = entry.id,
                            elapsedMillis = System.currentTimeMillis() - entry.startTime
                        )
                    }
                    startTicking()
                } else {
                    stopTicking()
                    _uiState.update { it.copy(isRunning = false, elapsedMillis = 0L, currentEntryId = null) }
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
    }

    fun onProjectSelected(id: Long?) {
        _uiState.update { it.copy(selectedProjectId = id, showProjectDropdown = false) }
    }

    fun toggleProjectDropdown() {
        _uiState.update { it.copy(showProjectDropdown = !it.showProjectDropdown) }
    }

    fun start() {
        viewModelScope.launch {
            startTimerUseCase(
                description = _uiState.value.description,
                projectId = _uiState.value.selectedProjectId
            )
        }
    }

    fun stop() {
        viewModelScope.launch { stopTimerUseCase() }
    }

    fun openEdit(entry: TimeEntryEntity) {
        _uiState.update { it.copy(entryToEdit = entry) }
    }

    fun closeEdit() {
        _uiState.update { it.copy(entryToEdit = null) }
    }
}
