package com.eden.alarmplus.main.ui.alarms.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eden.alarmplus.data.AlarmData
import com.eden.alarmplus.data.GroupData
import com.eden.alarmplus.main.ui.alarms.edit.alarm.AlarmSection
import com.eden.alarmplus.main.ui.alarms.edit.alarm.rememberAlarmSectionState
import com.eden.alarmplus.main.ui.alarms.edit.group.GroupSection
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(
    sheetState: SheetState,
    group: GroupData,
    onUpdate: (GroupData) -> Unit,
    onCancel: () -> Unit,
    initialEditMode: EditMode = EditMode.LIST,
    initialShowDiscardDialog: Boolean = false,
) {

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
    ) {
        var storedInfo by remember { mutableStateOf(group.info) }
        val storedAlarms = remember { group.alarms.toMutableList() }

        val alarmSectionState = rememberAlarmSectionState()
        fun applySelectedToStore() {
            val index = storedAlarms.indexOfFirst { it.uuid == alarmSectionState.value.uuid }
            if (index == -1)
                storedAlarms.add(alarmSectionState.value)
            else
                storedAlarms[index] = alarmSectionState.value
        }

        var editMode by remember { mutableStateOf(initialEditMode) }
        var showAlarmDiscardDialog by remember { mutableStateOf(initialShowDiscardDialog) }
        val editBackHandlerState = rememberEditBackHandlerState()

        AnimatedContent(
            targetState = editMode,
        ) { state ->
            when (state) {
                EditMode.LIST -> GroupSection(
                    info = storedInfo,
                    alarms = storedAlarms,
                    onAlarmSelected = { targetAlarm ->
                        alarmSectionState.value = targetAlarm
                        alarmSectionState.modified = false
                        editMode = EditMode.ITEM
                    },
                    onAlarmToggled = { toggledAlarm ->
                        storedAlarms[storedAlarms.indexOfFirst { it.uuid == toggledAlarm.uuid }] = toggledAlarm
                    },
                    onAddAlarm = {
                        alarmSectionState.value = AlarmData(
                            time = LocalTime.now(),
                            active = true,
                            features = emptyList(),
                        )
                        alarmSectionState.modified = true
                        editMode = EditMode.ITEM
                    },
                )
                EditMode.ITEM ->
                    AlarmSection(
                        state = alarmSectionState,
                        onSubmit = {
                            applySelectedToStore()
                            editMode = EditMode.LIST
                        },
                        onCancel = {
                            if (alarmSectionState.modified)
                                showAlarmDiscardDialog = true
                            else
                                editMode = EditMode.LIST
                        },
                    )
            }
        }

        if (showAlarmDiscardDialog)
            AlarmDiscardDialog(
                onSubmit = {
                    showAlarmDiscardDialog = false
                    applySelectedToStore()
                    editMode = EditMode.LIST
                },
                onDismiss = {
                    showAlarmDiscardDialog = false
                },
                onCancel = {
                    showAlarmDiscardDialog = false
                    editMode = EditMode.LIST
                }
            )

        EditBackHandler(
            state = editBackHandlerState,
            canBegin = editMode == EditMode.ITEM,
            onProgress = { thresholdReached ->
                editMode = if (thresholdReached)
                    EditMode.LIST
                else
                    EditMode.ITEM
            },
            onComplete = {
                if (alarmSectionState.modified) {
                    editMode = EditMode.ITEM
                    showAlarmDiscardDialog = true
                }
                else
                    editMode = EditMode.LIST
            },
            onCancel = {
                editMode = EditMode.ITEM
            }
        )


    }
}
