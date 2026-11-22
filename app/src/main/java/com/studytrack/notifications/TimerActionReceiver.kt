package com.studytrack.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.studytrack.data.repository.StudyTrackRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Handles actions shown on the ongoing timer notification. */
@AndroidEntryPoint
class TimerActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: StudyTrackRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TimerNotificationService.ACTION_STOP_TIMER) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                repository.getCurrentRunningEntryOnce()?.let { running ->
                    repository.updateTimeEntry(
                        running.copy(
                            endTime = System.currentTimeMillis(),
                            isRunning = false
                        )
                    )
                }
            } finally {
                TimerNotificationService.stop(context)
                pendingResult.finish()
            }
        }
    }
}
