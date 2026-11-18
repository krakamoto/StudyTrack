package com.studytrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.entities.ProjectEntity
import com.studytrack.data.repository.StudyTrackRepository
import com.studytrack.ui.components.PROJECT_COLORS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectsUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingProject: ProjectEntity? = null,
    val newName: String = "",
    val newColorHex: String = PROJECT_COLORS.first(),
    val nameError: String? = null
)

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: StudyTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllProjects().collect { list ->
                _uiState.update { it.copy(projects = list) }
            }
        }
    }

    fun openAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingProject = null,
                newName = "",
                newColorHex = PROJECT_COLORS.first(),
                nameError = null
            )
        }
    }

    fun openEditDialog(project: ProjectEntity) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingProject = project,
                newName = project.name,
                newColorHex = project.colorHex,
                nameError = null
            )
        }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                editingProject = null,
                newName = "",
                newColorHex = PROJECT_COLORS.first(),
                nameError = null
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(newName = value, nameError = null) }
    }

    fun onColorChange(hex: String) {
        _uiState.update { it.copy(newColorHex = hex) }
    }

    fun save() {
        val state = _uiState.value
        val name = state.newName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "Inserisci un nome per il progetto.") }
            return
        }
        val duplicate = state.projects.any { project ->
            project.id != state.editingProject?.id && project.name.equals(name, ignoreCase = true)
        }
        if (duplicate) {
            _uiState.update { it.copy(nameError = "Esiste già un progetto con questo nome.") }
            return
        }
        viewModelScope.launch {
            if (state.editingProject != null) {
                repository.updateProject(
                    state.editingProject.copy(name = name, colorHex = state.newColorHex)
                )
            } else {
                repository.insertProject(
                    ProjectEntity(name = name, colorHex = state.newColorHex, createdAt = System.currentTimeMillis())
                )
            }
            closeDialog()
        }
    }

    fun delete(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project.id)
        }
    }
}
