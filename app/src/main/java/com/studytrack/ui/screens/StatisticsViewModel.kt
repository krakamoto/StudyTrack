package com.studytrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.repository.StudyTrackRepository
import com.studytrack.data.settings.SettingsRepository
import com.studytrack.domain.usecases.CalculateProductivityUseCase
import com.studytrack.domain.usecases.ProductivityAnalytics
import com.studytrack.domain.usecases.StudySession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val selectedTab: Int = 0,                     // 0=Daily, 1=Weekly, 2=Monthly
    val analytics: ProductivityAnalytics = ProductivityAnalytics(),
    val projects: List<ProjectEntity> = emptyList()
) {
    fun projectName(id: Long?) = projects.firstOrNull { it.id == id }?.name
    fun projectColor(id: Long?) = projects.firstOrNull { it.id == id }?.colorHex
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StudyTrackRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateProductivity: CalculateProductivityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllEntries(),
                repository.getAllProjects(),
                settingsRepository.dailyGoalMinutes
            ) { entries, projects, dailyGoalMinutes ->
                Triple(entries, projects, dailyGoalMinutes)
            }
                .collect { (entries, projects, dailyGoalMinutes) ->
                    val sessions = entries
                        .filter { !it.isRunning && it.endTime != null }
                        .map { entry ->
                            StudySession(
                                startTime = entry.startTime,
                                endTime = entry.endTime ?: entry.startTime,
                                projectId = entry.projectId
                            )
                        }
                    val analytics = calculateProductivity(
                        sessions = sessions,
                        dailyGoalMinutes = dailyGoalMinutes
                    )
                    _uiState.update { current ->
                        current.copy(analytics = analytics, projects = projects)
                    }
                }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
}
