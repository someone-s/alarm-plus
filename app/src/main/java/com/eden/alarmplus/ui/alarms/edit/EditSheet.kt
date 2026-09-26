package com.eden.alarmplus.ui.alarms.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.eden.alarmplus.structure.alarm.AlarmData
import com.eden.alarmplus.structure.alarm.AlarmInfo
import com.eden.alarmplus.structure.group.GroupData
import com.eden.alarmplus.structure.group.emptyGroupData
import com.eden.alarmplus.ui.alarms.edit.alarm.AlarmSection
import com.eden.alarmplus.ui.alarms.edit.alarm.rememberAlarmSectionState
import com.eden.alarmplus.ui.alarms.edit.group.GroupSection
import com.eden.alarmplus.ui.alarms.edit.group.rememberGroupSectionState
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun rememberEditSheetState(): EditSheetState {
    return remember { EditSheetState() }
}
@Stable
class EditSheetState {
    var showSheet by mutableStateOf(false)
    var group by mutableStateOf(emptyGroupData)
    var modified by mutableStateOf(false)

    fun showSheet(
        group: GroupData,
        initialModified: Boolean = false,
    ) {
        this.group = group
        this.showSheet = true
        this.modified = initialModified
    }

    fun modifyGroup(
        group: GroupData,
    ) {
        this.group = group
        this.modified = true
    }

    fun resetGroup() {
        this.group = emptyGroupData
        this.modified = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(
    state: EditSheetState,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    val groupSectionState = rememberGroupSectionState(
        group = state.group,
    )
    val alarmSectionState = rememberAlarmSectionState()

    fun hideSheet() {
        coroutineScope.launch {
            sheetState.hide()
            state.showSheet = false
            state.resetGroup()
        }
    }

    if (state.showSheet)
        ModalBottomSheet(
            onDismissRequest = {
                if (alarmSectionState.modified)
                    groupSectionState.addOrUpdateAlarm(alarmSectionState.value)
                if (groupSectionState.modified)
                    state.modifyGroup(groupSectionState.group)
                onBack()
                hideSheet()
            },
            sheetState = sheetState,
        ) {
            var editMode by remember { mutableStateOf(EditMode.LIST) }
            val editBackHandlerState = rememberEditBackHandlerState()

            AnimatedContent(
                targetState = editMode,
            ) { animatedState ->
                when (animatedState) {
                    EditMode.LIST -> GroupSection(
                        state = groupSectionState,
                        onAddAlarm = {
                            alarmSectionState.setAlarmDraft(
                                AlarmData(
                                    info = AlarmInfo(
                                        time = LocalTime.now(),
                                    ),
                                    features = emptyList(),
                                )
                            )
                            editMode = EditMode.ITEM
                        },
                        onSelectAlarm = { targetAlarm ->
                            alarmSectionState.setAlarmCurrent(targetAlarm)
                            editMode = EditMode.ITEM
                        },
                        onDelete = {
                            if (groupSectionState.modified)
                                state.modifyGroup(groupSectionState.group)
                            onDelete()
                            hideSheet()
                        },
                        onBack = {
                            if (groupSectionState.modified)
                                state.modifyGroup(groupSectionState.group)
                            onBack()
                            hideSheet()
                        },
                    )
                    EditMode.ITEM ->
                        AlarmSection(
                            state = alarmSectionState,
                            onBack = {
                                if (alarmSectionState.modified)
                                    groupSectionState.addOrUpdateAlarm(alarmSectionState.value)
                                editMode = EditMode.LIST
                            },
                            onDelete = {
                                groupSectionState.removeIfExistAlarm(alarmSectionState.value)
                                editMode = EditMode.LIST
                            }
                        )
                }
            }

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
                    if (alarmSectionState.modified)
                        groupSectionState.addOrUpdateAlarm(alarmSectionState.value)
                    editMode = EditMode.LIST
                },
                onCancel = {
                    editMode = EditMode.ITEM
                }
            )


        }
}
