package com.eden.alarmplus.main.ui.alarms.edit

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException

@Composable
fun EditBackHandler(
    editMode: EditMode,
    setEditMode: (EditMode) -> Unit,
) {
    var backInProgress by remember { mutableStateOf(false) }
    PredictiveBackHandler(
        enabled = editMode == EditMode.ITEM || backInProgress,
    ) { progress ->

        backInProgress = true

        try {
            progress.collect { backEvent ->
                // On gesture in-progress
                setEditMode(
                    if (backEvent.progress > 0.3f)
                        EditMode.LIST
                    else
                        EditMode.ITEM
                )
            }

            // On gesture complete
            setEditMode(EditMode.LIST)
            backInProgress = false

        } catch (_: CancellationException) {

            // On gesture canceled
            setEditMode(EditMode.ITEM)
            backInProgress = false
        }
    }
}