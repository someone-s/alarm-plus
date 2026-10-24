package com.eden.alarmplus.ui.main.repeats.edit.group

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.feature.PinStartFeature
import com.eden.alarmplus.data.alarms.structure.alarm.getUniqueAlarmData
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleBeginFeature
import com.eden.alarmplus.data.alarms.structure.group.feature.ScheduleEndFeature
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
        )
}

@Composable
fun GroupSection(
    state: GroupSectionState,
    maximise: Boolean = false,
    onAddAlarm: () -> Unit,
    onSelectAlarm: (AlarmData) -> Unit,
    onDeleteAlarm: (AlarmData) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .let {
                if (maximise) it.fillMaxSize()
                else it
            }
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        LazyColumn(
            modifier = Modifier
                .let {
                    if (maximise) it.weight(1f)
                    else it
                },
            verticalArrangement = Arrangement.Center,
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
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                val dayOfWeekFlags = remember(state.info) {
                    state.info.getFeature<DayOfWeekFeature>()?.flags ?: DayOfWeekFlags.ALL
                }
                DayOfWeekGroup(
                    flags = dayOfWeekFlags,
                    onToggle = { flags, checked ->
                        state.info = state.info.shallowCopyReplaceFeature(DayOfWeekFeature(
                            if (checked) DayOfWeekFlags.merge(dayOfWeekFlags, flags)
                            else dayOfWeekFlags.copyClear(flags)
                        ))
                        state.modified = true
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                val scheduleBeginDate = remember(state.info) { state.info.getFeature<ScheduleBeginFeature>()?.date }
                val scheduleEndDate = remember(state.info) { state.info.getFeature<ScheduleEndFeature>()?.date }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ScheduleBeginButton(
                        modifier = Modifier.weight(1f),
                        date = scheduleBeginDate,
                        onChange = { date ->
                            state.info =
                                if (date != null) state.info.shallowCopyReplaceFeature(ScheduleBeginFeature(date))
                                else state.info.shallowCopyRemoveFeature<ScheduleBeginFeature>()
                            state.modified = true
                        },
                    )
                    ScheduleEndButton(
                        modifier = Modifier.weight(1f),
                        date = scheduleEndDate,
                        onChange = { date ->
                            state.info =
                                if (date != null) state.info.shallowCopyReplaceFeature(ScheduleEndFeature(date))
                                else state.info.shallowCopyRemoveFeature<ScheduleEndFeature>()
                            state.modified = true
                        },
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(2.dp))
            }
            item {
                AddButton(
                    shapes = ListItemDefaults.segmentedShapes(1, 2),
                    onClick = { onAddAlarm() }
                )
            }
            if (!maximise) item {
                Spacer(modifier = Modifier.height(16.dp))
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
        if (maximise) {
            Spacer(modifier = Modifier.height(16.dp))
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


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(widthDp = 500, heightDp = 300, showSystemUi = true)
@Composable
fun GroupSectionPreview() {
    val sheetState = rememberBottomSheetState(SheetValue.Expanded)
    val groupSectionState = rememberGroupSectionState(
        GroupData(
            info = GroupInfo(
                name = "Morning Alarm",
                features = listOf(
                    DayOfWeekFeature(DayOfWeekFlags.merge(
                        DayOfWeekFlags.WEDNESDAY,
                        DayOfWeekFlags.FRIDAY
                    ))
                )
            ),
            alarms = listOf(
                getUniqueAlarmData(),
                getUniqueAlarmData(),
            ),
        )
    )

    Scaffold {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {},
        ) {
            GroupSection(
                state = groupSectionState,
                onAddAlarm = {},
                onSelectAlarm = {},
                onDeleteAlarm = {},
                onDelete = {},
                onBack = {},
            )
        }
    }
}
