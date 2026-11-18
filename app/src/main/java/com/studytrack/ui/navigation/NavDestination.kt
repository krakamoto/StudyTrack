package com.studytrack.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Timer : NavDestination("timer", "Timer", Icons.Filled.Timer)
    data object History : NavDestination("history", "History", Icons.Filled.History)
    data object Statistics : NavDestination("statistics", "Statistics", Icons.Filled.BarChart)
    data object Projects : NavDestination("projects", "Projects", Icons.Filled.FolderOpen)

    data object EditEntry : NavDestination("edit_entry/{entryId}", "Edit Entry", Icons.Filled.Edit) {
        fun route(entryId: Long) = "edit_entry/$entryId"
    }
}

val bottomNavDestinations = listOf(
    NavDestination.Timer,
    NavDestination.History,
    NavDestination.Statistics,
    NavDestination.Projects
)
