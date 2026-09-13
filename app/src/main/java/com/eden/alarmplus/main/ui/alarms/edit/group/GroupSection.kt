package com.eden.alarmplus.main.ui.alarms.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.alarm.AlarmData
import com.eden.alarmplus.data.group.GroupData


@Composable
fun rememberGroupSectionState(
    group: GroupData,
): GroupSectionState {
    return remember(group) {
        GroupSectionState(
            initialGroup = group,
        )
    }
}
@Stable
class GroupSectionState(
    initialGroup: GroupData,
) {
    var info by mutableStateOf(initialGroup.info)
    val alarms = initialGroup.alarms.map { Pair(it.info.uuid, it) }.toMutableStateMap()
    var modified by mutableStateOf(false)
    var showSaveDialog by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)

    fun addOrUpdateAlarm(alarm: AlarmData) {
        alarms[alarm.info.uuid] = alarm
        modified = true
    }

    fun removeIfExistAlarm(alarm: AlarmData) {
        val result = alarms.remove(alarm.info.uuid)
        modified = result != null
    }

    fun getDiscardConfirmation(): Boolean {
        if (modified) {
            showSaveDialog = true
            return false
        }
        else
            return true
    }

    val group: GroupData
        get() = GroupData(
            info = this.info,
            alarms = this.alarms.values.sortedBy { alarm -> alarm.info.time }.toList(),
        )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GroupSection(
    state: GroupSectionState,
    onAddAlarm: () -> Unit,
    onSelectAlarm: (AlarmData) -> Unit,
    onApply: (GroupData) -> Unit,
    onDelete: (GroupData) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        GroupTitle(
            title = state.info.name,
            setTitle = { newName ->
                state.info = state.info.shallowCopy(newName)
                state.modified = true
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        state.alarms.values
            .sortedBy { it.info.time }
            .forEachIndexed { index, alarm ->
                AlarmItem(
                    shapes = ListItemDefaults.segmentedShapes(
                        index,
                        state.alarms.size + 1,
                    ),
                    alarm = alarm,
                    onSelect = onSelectAlarm,
                    onDelete = { targetAlarm ->
                        state.removeIfExistAlarm(targetAlarm)
                    },
                )
            }
        AddButton(
            shapes = ListItemDefaults.segmentedShapes(1, 2),
            onClick = { onAddAlarm() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        NavigateGroup(
            hasChanges = state.modified,
            onApply = {
                onApply(state.group)
            },
            onDelete = {
                state.showDeleteDialog = true
            },
        )
    }

    if (state.showDeleteDialog)
        DeleteDialog(
            onConfirm = {
                onDelete(state.group)
                state.showDeleteDialog = false
            },
            onDismiss = {
                state.showDeleteDialog = false
            },
        )

    if (state.showSaveDialog)
        SaveDialog(
            onConfirm = {
                onApply(state.group)
                state.showSaveDialog = false
            },
            onDismiss = {
                onBack()
                state.showSaveDialog = false
            },
        )
}