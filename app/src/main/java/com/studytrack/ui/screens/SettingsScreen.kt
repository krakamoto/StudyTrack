package com.studytrack.ui.screens

import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studytrack.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let(viewModel::exportTo)
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(viewModel::prepareImport)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSectionTitle("BACKUP E RIPRISTINO")
            }
            item {
                SettingsCard(
                    icon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                    title = "Esporta i tuoi dati",
                    description = "Crea un backup JSON con progetti e sessioni di studio. Conservalo in un luogo sicuro."
                ) {
                    OutlinedButton(
                        onClick = {
                            val name = "studytrack-backup-${
                                SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
                            }.json"
                            exportLauncher.launch(name)
                        },
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Esporta backup")
                    }
                }
            }
            item {
                SettingsCard(
                    icon = { Icon(Icons.Filled.FileUpload, contentDescription = null) },
                    title = "Importa un backup",
                    description = "Ripristina un backup creato da StudyTrack. L'operazione sostituisce tutti i dati attuali."
                ) {
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(
                                arrayOf("application/json", "text/plain", "application/octet-stream")
                            )
                        },
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scegli file")
                    }
                }
            }
            item {
                SettingsSectionTitle("DATI LOCALI")
            }
            item {
                SettingsCard(
                    icon = {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = "Elimina tutti i dati",
                    description = "Rimuove definitivamente progetti e sessioni salvati sul dispositivo."
                ) {
                    Button(
                        onClick = viewModel::askClearData,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Elimina dati")
                    }
                }
            }
            item {
                SettingsSectionTitle("INFORMAZIONI")
            }
            item {
                SettingsCard(
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    title = "StudyTrack ${BuildConfig.VERSION_NAME}",
                    description = "Timer e statistiche per lo studio, con tutti i dati conservati offline sul dispositivo."
                )
            }
            item {
                Text(
                    text = "StudyTrack non invia dati a server esterni.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    state.pendingImportPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = viewModel::cancelImport,
            title = { Text("Ripristinare il backup?") },
            text = {
                Text(
                    "Il backup contiene ${preview.entryCount} sessioni e " +
                        "${preview.projectCount} progetti, esportati il " +
                        "${DateFormat.getDateFormat(androidx.compose.ui.platform.LocalContext.current)
                            .format(Date(preview.exportedAt))}.\n\n" +
                        "Tutti i dati attuali verranno sostituiti."
                )
            },
            confirmButton = {
                Button(onClick = viewModel::confirmImport) { Text("Ripristina") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelImport) { Text("Annulla") }
            }
        )
    }

    if (state.showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::cancelClearData,
            title = { Text("Eliminare tutti i dati?") },
            text = { Text("Questa operazione non può essere annullata. Esporta prima un backup se vuoi conservare i dati.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmClearData) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelClearData) { Text("Annulla") }
            }
        )
    }

    if (state.isBusy) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    content: @Composable (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                icon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content?.invoke()
        }
    }
}
