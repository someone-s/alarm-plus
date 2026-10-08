package com.eden.alarmplus.ui.main.alarms.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.feature.PinStartFeature
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import java.time.LocalTime
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid


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
    val sortedAlarms = initialGroup.alarms.toMutableStateList()
    var dayOfWeekFlags by mutableStateOf(initialGroup.dayOfWeekFlags)
    var modified by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)

    fun addOrUpdateAlarm(alarm: AlarmData) {
        val index = sortedAlarms.indexOfFirst { it.info.uuid == alarm.info.uuid }

        val oldAlarm = if (index >= 0) sortedAlarms[index] else null

        val editedAndPotentiallyReordered = if (index >= 0) {
            sortedAlarms[index] = alarm
            alarm != oldAlarm
        }
        else {
            sortedAlarms.add(alarm)
            true
        }

        val previousPin = oldAlarm?.getFeature<PinStartFeature>() != null
        val currentPin = alarm.getFeature<PinStartFeature>() != null
        if (!previousPin && currentPin) {

            val currentPinIndices = sortedAlarms
                .withIndex()
                .filter { it.value.info.uuid != alarm.info.uuid }
                .filter { it.value.getFeature<PinStartFeature>() != null }
                .map { it.index }
                .toList()

            currentPinIndices.forEach { index ->
                sortedAlarms[index] = sortedAlarms[index].shallowCopyRemoveFeature<PinStartFeature>()
            }
        }
        val pinChangedAndPotentiallyReordered = currentPin != previousPin

        val pinTime = sortedAlarms.firstOrNull { it.getFeature<PinStartFeature>() != null }?.info?.time ?: LocalTime.MIDNIGHT
        sortedAlarms.sortBy {
            val alarmTime = it.info.time
            if (alarmTime < pinTime)
                alarmTime.toNanoOfDay() + 24.hours.inWholeNanoseconds
            else
                alarmTime.toNanoOfDay()
        }
        modified = modified || editedAndPotentiallyReordered || pinChangedAndPotentiallyReordered
    }

    fun removeIfExistAlarm(alarmUuid: Uuid) {
        val unpinnedAndPotentiallyReordered = sortedAlarms
            .filter{ it.info.uuid == alarmUuid }
            .any { it.getFeature<PinStartFeature>() != null }

        val removedAndPotentiallyReordered = sortedAlarms.removeIf { it.info.uuid == alarmUuid }

        val pinTime = sortedAlarms.firstOrNull { it.getFeature<PinStartFeature>() != null }?.info?.time ?: LocalTime.MIDNIGHT
        sortedAlarms.sortBy {
            val alarmTime = it.info.time
            if (alarmTime < pinTime)
                alarmTime.toNanoOfDay() + 24.hours.inWholeNanoseconds
            else
                alarmTime.toNanoOfDay()
        }

        modified = modified || unpinnedAndPotentiallyReordered || removedAndPotentiallyReordered
    }

    val group: GroupData
        get() = GroupData(
            info = this.info,
            alarms = this.sortedAlarms.toList(),
            dayOfWeekFlags = this.dayOfWeekFlags,
        )
}

@Composable
fun GroupSection(
    state: GroupSectionState,
    onAddAlarm: () -> Unit,
    onSelectAlarm: (AlarmData) -> Unit,
    onDeleteAlarm: (AlarmData) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
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
                onDelete = onDeleteAlarm,
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