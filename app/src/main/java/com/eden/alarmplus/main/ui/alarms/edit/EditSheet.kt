package com.eden.alarmplus.main.ui.alarms.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eden.alarmplus.data.AlarmData
import com.eden.alarmplus.data.GroupData
import com.eden.alarmplus.data.emptyAlarmData
import com.eden.alarmplus.main.ui.alarms.edit.alarm.AlarmSection
import com.eden.alarmplus.main.ui.alarms.edit.group.GroupSection
import java.time.LocalTime

private data class SelectedAlarmState(
    val value: AlarmData,
    val modified: Boolean,
)

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

        var selectedAlarm by remember { mutableStateOf(SelectedAlarmState(emptyAlarmData, false)) }
        var showDiscardDialog by remember { mutableStateOf(initialShowDiscardDialog) }
        fun applySelectedToStore() {
            val index = storedAlarms.indexOfFirst { it.uuid == selectedAlarm.value.uuid }
            if (index == -1)
                storedAlarms.add(selectedAlarm.value)
            else
                storedAlarms[index] = selectedAlarm.value
        }

        var editMode by remember { mutableStateOf(initialEditMode) }

        AnimatedContent(
            targetState = editMode,
        ) { state ->
            when (state) {
                EditMode.LIST -> GroupSection(
                    info = storedInfo,
                    alarms = storedAlarms,
                    onAlarmSelected = { targetAlarm ->
                        selectedAlarm = SelectedAlarmState(targetAlarm, false)
                        editMode = EditMode.ITEM
                    },
                    onAlarmToggled = { toggledAlarm ->
                        storedAlarms[storedAlarms.indexOfFirst { it.uuid == toggledAlarm.uuid }] = toggledAlarm
                    },
                    onAddAlarm = {
                        selectedAlarm = SelectedAlarmState(
                            AlarmData(
                                time = LocalTime.now(),
                                active = true,
                                features = emptyList(),
                            ),
                            true,
                        )
                        editMode = EditMode.ITEM
                    },
                )
                EditMode.ITEM -> AlarmSection(
                    alarm = selectedAlarm.value,
                    onUpdate = { updatedAlarm ->
                        selectedAlarm = SelectedAlarmState(updatedAlarm, true)
                    },
                    onSubmit = {
                        applySelectedToStore()
                        editMode = EditMode.LIST
                    },
                    onCancel = {
                        if (selectedAlarm.modified)
                            showDiscardDialog = true
                        else
                            editMode = EditMode.LIST
                    }
                )
            }
        }

        if (showDiscardDialog)
            AlertDialog(
                onDismissRequest = {
                    showDiscardDialog = false
                    editMode = EditMode.LIST
                },
                title = { Text(text = "Discard changes?") },
                text = { Text(text = "Changes to this specific alarm will be reverted.") },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDiscardDialog = false
                            editMode = EditMode.LIST
                          },
                        content = { Text(text = "Discard") },
                    )
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = {
                            applySelectedToStore()
                            showDiscardDialog = false
                            editMode = EditMode.LIST
                        },
                        content = { Text(text = "Apply") },
                    )
                }
            )

        EditBackHandler(
            editMode = editMode,
            setEditMode = { editMode = it }
        )


    }
}
