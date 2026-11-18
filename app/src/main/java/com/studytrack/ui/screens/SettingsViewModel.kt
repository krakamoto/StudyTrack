package com.studytrack.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studytrack.data.backup.BackupPreview
import com.studytrack.data.backup.BackupRepository
import com.studytrack.data.backup.StudyTrackBackup
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val pendingImport: StudyTrackBackup? = null,
    val pendingImportPreview: BackupPreview? = null,
    val showClearDataConfirm: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            try {
                val backup = backupRepository.createBackup()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                        writer.write(backup)
                    } ?: error("Impossibile aprire il file di destinazione.")
                }
                _uiState.update { it.copy(message = "Backup esportato correttamente.") }
            } catch (error: Exception) {
                _uiState.update { it.copy(message = error.message ?: "Esportazione non riuscita.") }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun prepareImport(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Impossibile leggere il file selezionato.")
                }
                val backup = backupRepository.parseBackup(content)
                _uiState.update {
                    it.copy(
                        pendingImport = backup,
                        pendingImportPreview = BackupPreview(
                            projectCount = backup.projects.size,
                            entryCount = backup.timeEntries.size,
                            exportedAt = backup.exportedAt
                        )
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(message = error.message ?: "Importazione non riuscita.")
                }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun confirmImport() {
        val backup = _uiState.value.pendingImport ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, pendingImport = null, pendingImportPreview = null) }
            try {
                backupRepository.restoreBackup(backup)
                _uiState.update { it.copy(message = "Backup ripristinato correttamente.") }
            } catch (error: Exception) {
                _uiState.update { it.copy(message = error.message ?: "Ripristino non riuscito.") }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun cancelImport() {
        _uiState.update { it.copy(pendingImport = null, pendingImportPreview = null) }
    }

    fun askClearData() {
        _uiState.update { it.copy(showClearDataConfirm = true) }
    }

    fun cancelClearData() {
        _uiState.update { it.copy(showClearDataConfirm = false) }
    }

    fun confirmClearData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, showClearDataConfirm = false) }
            try {
                backupRepository.clearAll()
                _uiState.update { it.copy(message = "Tutti i dati locali sono stati eliminati.") }
            } catch (error: Exception) {
                _uiState.update { it.copy(message = error.message ?: "Eliminazione non riuscita.") }
            } finally {
                _uiState.update { it.copy(isBusy = false) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
