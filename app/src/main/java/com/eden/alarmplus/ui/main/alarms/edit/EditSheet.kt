package com.eden.alarmplus.ui.main.alarms.edit

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.alarm.getUniqueAlarmData
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.group.emptyGroupData
import com.eden.alarmplus.data.alarms.structure.group.getUniqueGroupData
import com.eden.alarmplus.ui.main.alarms.edit.alarm.AlarmSection
import com.eden.alarmplus.ui.main.alarms.edit.alarm.rememberAlarmSectionState
import com.eden.alarmplus.ui.main.alarms.edit.group.GroupSection
import com.eden.alarmplus.ui.main.alarms.edit.group.rememberGroupSectionState
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun rememberEditSheetState(
    initialShowSheet: Boolean = false,
    initialGroup: GroupData = getUniqueGroupData(),
): EditSheetState {
    return remember { EditSheetState(
        initialShowSheet = initialShowSheet,
        initialGroup = initialGroup,
    ) }
}
@Stable
class EditSheetState(
    initialShowSheet: Boolean = false,
    initialGroup: GroupData = getUniqueGroupData(),
) {
    var showSheet by mutableStateOf(initialShowSheet)
    var group by mutableStateOf(initialGroup)
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
    val sheetState = rememberBottomSheetState(
        if (state.showSheet) SheetValue.Expanded
        else SheetValue.Hidden
    )

    val groupSectionState = rememberGroupSectionState(
        group = state.group,
    )
    val alarmSectionState = rememberAlarmSectionState(
        key = state.group
    )

    val hapticFeedback = LocalHapticFeedback.current

    fun hideSheet() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
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
                        onDeleteAlarm = { targetAlarm ->
                            groupSectionState.removeIfExistAlarm(targetAlarm.info.uuid)
                            alarmSectionState.clearAlarm()
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
                                groupSectionState.removeIfExistAlarm(alarmSectionState.value.info.uuid)
                                alarmSectionState.clearAlarm()
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
