package com.studytrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.entities.TimeEntryEntity
import com.studytrack.data.repository.StudyTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HistoryUiState(
    val groupedEntries: Map<String, List<TimeEntryEntity>> = emptyMap(),
    val projects: List<ProjectEntity> = emptyList(),
    val filterProjectId: Long? = null,
    val showFilterDropdown: Boolean = false
) {
    fun projectFor(id: Long?) = projects.firstOrNull { it.id == id }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: StudyTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displaySdf = SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault())

    init {
        viewModelScope.launch {
            combine(
                repository.getAllEntries(),
                repository.getAllProjects(),
                _uiState.map { it.filterProjectId }.distinctUntilChanged()
            ) { entries, projects, filterProject ->
                val filtered = if (filterProject != null) {
                    entries.filter { it.projectId == filterProject }
                } else entries

                val grouped = filtered
                    .filter { !it.isRunning }
                    .groupBy { entry ->
                        val date = Date(entry.startTime)
                        displaySdf.format(date)
                    }
                Triple(grouped, projects, filterProject)
            }.collect { (grouped, projects, _) ->
                _uiState.update { it.copy(groupedEntries = grouped, projects = projects) }
            }
        }
    }

    fun setFilter(projectId: Long?) {
        _uiState.update { it.copy(filterProjectId = projectId, showFilterDropdown = false) }
    }

    fun toggleFilterDropdown() {
        _uiState.update { it.copy(showFilterDropdown = !it.showFilterDropdown) }
    }

    fun deleteEntry(entry: TimeEntryEntity) {
        viewModelScope.launch {
            repository.deleteTimeEntry(entry.id)
        }
    }
}
