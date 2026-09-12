package com.eden.alarmplus.main.ui.alarms.edit

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException

@Composable
fun rememberEditBackHandlerState(): EditBackHandlerState {
    return remember { EditBackHandlerState() }
}

@Stable
 class EditBackHandlerState {
    internal var backInProgress by mutableStateOf(false)
}

@Composable
fun EditBackHandler(
    state: EditBackHandlerState,
    canBegin: Boolean,
    onProgress: (thresholdReached: Boolean) -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    threshold: Float = 0.3f,
) {
    PredictiveBackHandler(
        enabled = canBegin || state.backInProgress,
    ) { progress ->

        state.backInProgress = true

        try {
            progress.collect { backEvent ->
                // On gesture in-progress
                onProgress(backEvent.progress > threshold)
            }

            // On gesture complete
            onComplete()
            state.backInProgress = false

        } catch (_: CancellationException) {

            // On gesture canceled
            onCancel()
            state.backInProgress = false
        }
    }
}