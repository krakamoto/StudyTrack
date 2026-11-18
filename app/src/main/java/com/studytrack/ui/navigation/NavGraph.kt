package com.studytrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.studytrack.ui.screens.*

@Composable
fun StudyTrackNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Timer.route,
        modifier = modifier
    ) {
        composable(NavDestination.Timer.route) {
            TimerScreen(
                onNavigateToEdit = { entryId ->
                    navController.navigate(NavDestination.EditEntry.route(entryId))
                }
            )
        }

        composable(NavDestination.History.route) {
            HistoryScreen(
                onNavigateToEdit = { entryId ->
                    navController.navigate(NavDestination.EditEntry.route(entryId))
                }
            )
        }

        composable(NavDestination.Statistics.route) {
            StatisticsScreen()
        }

        composable(NavDestination.Projects.route) {
            ProjectsScreen()
        }

        composable(
            route = NavDestination.EditEntry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) {
            EditTimeEntryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
