package com.studytrack.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity  // kept for UiState
import com.studytrack.data.repository.StudyTrackRepository
import com.studytrack.domain.usecases.UpdateTimeEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTimeEntryUiState(
    val entry: TimeEntryEntity? = null,
    val description: String = "",
    val selectedProjectId: Long? = null,
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isDone: Boolean = false
)

@HiltViewModel
class EditTimeEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StudyTrackRepository,
    private val updateTimeEntryUseCase: UpdateTimeEntryUseCase
) : ViewModel() {

    private val entryId: Long = savedStateHandle["entryId"] ?: -1L

    private val _uiState = MutableStateFlow(EditTimeEntryUiState())
    val uiState: StateFlow<EditTimeEntryUiState> = _uiState.asStateFlow()

    init {
        loadEntry()
        loadProjects()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            val entry = repository.getTimeEntryById(entryId) ?: return@launch
            _uiState.update {
                it.copy(
                    entry = entry,
                    description = entry.description,
                    selectedProjectId = entry.projectId,
                    startTime = entry.startTime,
                    endTime = entry.endTime
                )
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            repository.getAllProjects().collect { list ->
                _uiState.update { it.copy(projects = list) }
            }
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onProjectSelected(id: Long?) {
        _uiState.update { it.copy(selectedProjectId = id) }
    }

    fun onStartTimeChange(millis: Long) {
        _uiState.update { it.copy(startTime = millis) }
    }

    fun onEndTimeChange(millis: Long) {
        _uiState.update { it.copy(endTime = millis) }
    }

    fun save() {
        val state = _uiState.value
        val entry = state.entry ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            updateTimeEntryUseCase(
                id = entry.id,
                description = state.description,
                projectId = state.selectedProjectId,
                startTime = state.startTime,
                endTime = state.endTime,
                clearEndTime = state.endTime == null
            )
            _uiState.update { it.copy(isSaving = false, isDone = true) }
        }
    }

    fun delete() {
        val entry = _uiState.value.entry ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            repository.deleteTimeEntry(entry.id)
            _uiState.update { it.copy(isDeleting = false, isDone = true) }
        }
    }
}
