package com.eden.alarmplus.main.ui.alarms.edit

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eden.alarmplus.data.GroupData
import com.eden.alarmplus.data.emptyAlarmData
import com.eden.alarmplus.main.ui.alarms.edit.alarm.AlarmSection
import com.eden.alarmplus.main.ui.alarms.edit.group.GroupSection
import kotlinx.coroutines.CancellationException


private enum class EditMode {
    LIST,
    ITEM,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(
    sheetState: SheetState,
    group: GroupData,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {

        var editMode by remember { mutableStateOf(EditMode.LIST) }
        var selectedAlarm by remember { mutableStateOf(emptyAlarmData) }

        AnimatedContent(
            targetState = editMode,
        ) { state ->
            when (state) {
                EditMode.LIST -> GroupSection(
                    group = group,
                    onAlarmSelected = { alarm ->
                        selectedAlarm = alarm
                        editMode = EditMode.ITEM
                    }
                )
                EditMode.ITEM -> AlarmSection(
                    alarm = selectedAlarm,
                    onBack = {
                        editMode = EditMode.LIST
                    }
                )
            }
        }

        var backInProgress by remember { mutableStateOf(false) }
        PredictiveBackHandler(
            enabled = editMode == EditMode.ITEM || backInProgress,
        ) { progress ->

            backInProgress = true

            try {
                progress.collect { backEvent ->
                    // On gesture in-progress
                    editMode = if (backEvent.progress > 0.3f)
                        EditMode.LIST
                    else
                        EditMode.ITEM
                }

                // On gesture complete
                editMode = EditMode.LIST
                backInProgress = false

            } catch (_: CancellationException) {

                // On gesture canceled
                editMode = EditMode.ITEM
                backInProgress = false
            }
        }



    }
}

