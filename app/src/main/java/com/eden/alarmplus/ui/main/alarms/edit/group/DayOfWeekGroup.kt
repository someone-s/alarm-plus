package com.eden.alarmplus.ui.main.alarms.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags

@Composable
fun DayOfWeekGroup(
    flags: DayOfWeekFlags,
    onToggle: (DayOfWeekFlags, Boolean) -> Unit,
) {
    val days = listOf(
        Pair(DayOfWeekFlags.MONDAY,     stringResource(R.string.ui_main_alarms_edit_group_dayofweekgroup_monday)),
        Pair(DayOfWeekFlags.TUESDAY,    stringResource(R.string.ui_main_alarms_edit_group_dayofweekgroup_tuesday)),
        Pair(DayOfWeekFlags.WEDNESDAY,  stringResource(R.string.ui_main_alarms_edit_group_dayofweekgroup_wednesday)),
        Pair(DayOfWeekFlags.THURSDAY,   stringResource(R.string.ui_main_alarms_edit_group_dayofweekgroup_thursday)),
        Pair(DayOfWeekFlags.FRIDAY,     stringResource(R.string.ui_main_alarms_edit_group_dayofweekgroup_friday)),
        Pair(DayOfWeekFlags.SATURDAY,   stringResource(R.string.ui_main_alarms_edit_group_dayofweekgroup_saturday)),
        Pair(DayOfWeekFlags.SUNDAY,     stringResource(R.string.ui_main_alarms_edit_group_dayofweekgroup_sunday))
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        days.forEach { (flag, text) ->
            Box(Modifier.weight(1f)) {
                ToggleButton(
                    checked = flags.any(flag),
                    onCheckedChange = { onToggle(flag, it) },
                    content = { Text(text) },
                    modifier = Modifier.aspectRatio(1f),
                )
            }
        }
    }
}