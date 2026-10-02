package com.eden.alarmplus.ui.main.alarms.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.ui.common.FormatViewModel
import com.eden.alarmplus.ui.common.FormatViewModelProvider


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
    val sortedAlarms = initialGroup.alarms.sortedBy { it.info.time }.toMutableStateList()
    var modified by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)

    fun addOrUpdateAlarm(alarm: AlarmData) {
        sortedAlarms.add(alarm)
        sortedAlarms.sortBy { it.info.time }
        modified = true
    }

    fun removeIfExistAlarm(alarm: AlarmData) {
        val result = sortedAlarms.removeIf { it.info.uuid == alarm.info.uuid }
        modified = modified || result
    }

    val group: GroupData
        get() = GroupData(
            info = this.info,
            alarms = this.sortedAlarms.toList(),
        )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GroupSection(
    state: GroupSectionState,
    onAddAlarm: () -> Unit,
    onSelectAlarm: (AlarmData) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    formatViewModel: FormatViewModel = viewModel(factory = FormatViewModelProvider.Factory),
) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            GroupTitle(
                title = state.info.name,
                setTitle = { newName ->
                    state.info = state.info.shallowCopy(newName)
                    state.modified = true
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        itemsIndexed(state.sortedAlarms) { index, alarm ->
            AlarmItem(
                modifier = Modifier.animateItem(),
                shapes = ListItemDefaults.segmentedShapes(
                    index,
                    state.sortedAlarms.size + 1,
                ),
                alarm = alarm,
                onSelect = onSelectAlarm,
                onDelete = { targetAlarm ->
                    state.removeIfExistAlarm(targetAlarm)
                },
                formatViewModel = formatViewModel,
            )
        }
        item {
            AddButton(
                shapes = ListItemDefaults.segmentedShapes(1, 2),
                onClick = { onAddAlarm() }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            NavigateGroup(
                onBack = {
                    onBack()
                },
                onDelete = {
                    state.showDeleteDialog = true
                },
            )
        }
    }

    if (state.showDeleteDialog)
        DeleteDialog(
            onConfirm = {
                onDelete()
                state.showDeleteDialog = false
            },
            onDismiss = {
                state.showDeleteDialog = false
            },
        )
}