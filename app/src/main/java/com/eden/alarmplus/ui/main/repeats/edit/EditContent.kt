package com.eden.alarmplus.ui.main.repeats.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.ui.main.repeats.edit.alarm.AlarmSection
import com.eden.alarmplus.ui.main.repeats.edit.alarm.AlarmSectionState
import com.eden.alarmplus.ui.main.repeats.edit.group.GroupSection
import com.eden.alarmplus.ui.main.repeats.edit.group.GroupSectionState
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun rememberEditContentState(
    group: GroupData,
): EditContentState {
    return remember(group) { EditContentState(
        groupSectionState = GroupSectionState(group),
        alarmSectionState = AlarmSectionState()
    ) }
}
@Stable
class EditContentState(
    val groupSectionState: GroupSectionState,
    val alarmSectionState: AlarmSectionState,
) {
    val modified: Boolean get() = alarmSectionState.modified || groupSectionState.modified

    val group: GroupData get() {
        if (alarmSectionState.modified)
            groupSectionState.addOrUpdateAlarm(alarmSectionState.value)
        return groupSectionState.group
    }
}

@Composable
fun EditContent(
    editContentState: EditContentState,
    maximise: Boolean = false,
    currentDateTime: LocalDateTime,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val groupSectionState = editContentState.groupSectionState
    val alarmSectionState = editContentState.alarmSectionState

    var editMode by remember { mutableStateOf(EditMode.LIST) }
    val editBackHandlerState = rememberEditBackHandlerState()

    AnimatedContent(
        targetState = editMode,
    ) { animatedState ->
        when (animatedState) {
            EditMode.LIST -> GroupSection(
                state = groupSectionState,
                maximise = maximise,
                onAddAlarm = {
                    alarmSectionState.setAlarmDraft(
                        AlarmData(
                            info = AlarmInfo(
                                time = currentDateTime.toLocalTime(),
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
                    onDelete()
                },
                onBack = {
                    onBack()
                },
            )

            EditMode.ITEM -> AlarmSection(
                state = alarmSectionState,
                maximise = maximise,
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