package com.studytrack.ui.components

import android.app.TimePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimePicked: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute -> onTimePicked(hour, minute) },
            initialHour,
            initialMinute,
            true // 24-hour format
        ).apply {
            setOnDismissListener { onDismiss() }
        }
    }
    DisposableEffect(Unit) {
        dialog.show()
        onDispose { dialog.dismiss() }
    }
}
