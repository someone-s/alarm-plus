package com.eden.alarmplus.main.ui.alarms.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.alarm.AlarmData
import com.eden.alarmplus.data.group.GroupInfo

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GroupSection(
    info: GroupInfo,
    alarms: List<AlarmData>,
    onAlarmSelected: (AlarmData) -> Unit,
    onAddAlarm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        GroupTitle(
            title = info.name ?: "Unnamed",
        )
        Spacer(modifier = Modifier.height(24.dp))
        alarms
            .sortedBy { it.info.time }
            .forEachIndexed { index, alarm ->
                AlarmItem(
                    shape = ListItemDefaults.segmentedShapes(
                        index,
                        alarms.size + 1,
                    ),
                    alarm = alarm,
                    onClick = { onAlarmSelected(alarm) },
                )
            }
        AddButton(
            shapes = ListItemDefaults.segmentedShapes(1, 2),
            onClick = { onAddAlarm() }
        )
    }
}