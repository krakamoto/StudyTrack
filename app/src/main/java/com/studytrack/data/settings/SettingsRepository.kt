package com.studytrack.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "study_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dailyGoalKey = intPreferencesKey("daily_goal_minutes")

    val dailyGoalMinutes: Flow<Int> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            (preferences[dailyGoalKey] ?: DEFAULT_DAILY_GOAL_MINUTES)
                .coerceIn(MIN_DAILY_GOAL_MINUTES, MAX_DAILY_GOAL_MINUTES)
        }
        .distinctUntilChanged()

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[dailyGoalKey] = minutes.coerceIn(
                MIN_DAILY_GOAL_MINUTES,
                MAX_DAILY_GOAL_MINUTES
            )
        }
    }

    companion object {
        const val DEFAULT_DAILY_GOAL_MINUTES = 120
        const val MIN_DAILY_GOAL_MINUTES = 15
        const val MAX_DAILY_GOAL_MINUTES = 600
        val QUICK_GOAL_OPTIONS = listOf(30, 60, 90, 120, 180, 240)
    }
}
