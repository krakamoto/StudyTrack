package com.studytrack.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.studytrack.MainActivity
import com.studytrack.R
import com.studytrack.data.entities.TimeEntryEntity
import com.studytrack.data.repository.StudyTrackRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Keeps the running study session visible as an ongoing notification.
 * The notification uses the system chronometer, so it updates without a
 * per-second notification refresh.
 */
@AndroidEntryPoint
class TimerNotificationService : Service() {

    @Inject
    lateinit var repository: StudyTrackRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            buildNotification(entry = null, projectName = null)
        )

        serviceScope.launch {
            repository.getCurrentRunningEntry().collectLatest { entry ->
                if (entry == null) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collectLatest
                }
                val projectName = entry.projectId?.let { projectId ->
                    repository.getProjectById(projectId)?.name
                }
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, buildNotification(entry, projectName))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timer di studio",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mostra la sessione di studio in corso"
            setShowBadge(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        entry: TimeEntryEntity?,
        projectName: String?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_STOP_TIMER,
            Intent(this, TimerActionReceiver::class.java).setAction(ACTION_STOP_TIMER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = entry?.description?.takeIf { it.isNotBlank() }
            ?: "Sessione di studio in corso"
        val text = projectName?.let { "Progetto: $it" }
            ?: "StudyTrack · tocca per aprire"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .apply {
                if (entry != null) {
                    setUsesChronometer(true)
                    setWhen(entry.startTime)
                }
            }
            .addAction(R.drawable.ic_stat_timer, "Ferma", stopPendingIntent)
            .build()
    }

    companion object {
        const val ACTION_STOP_TIMER = "com.studytrack.action.STOP_TIMER"
        private const val CHANNEL_ID = "study_timer"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_OPEN_APP = 10
        private const val REQUEST_STOP_TIMER = 11

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerNotificationService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimerNotificationService::class.java))
        }
    }
}
