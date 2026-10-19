package com.eden.alarmplus.ui.main.alarms.edit.group

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.ui.common.format.LocalFormat
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ScheduleBeginButton(
    modifier: Modifier = Modifier,
    date: LocalDate?,
    onChange: (LocalDate?) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    FilledTonalButton(
        modifier = modifier,
        shape = ButtonDefaults.squareShape,
        colors =
            if (date != null) ButtonDefaults.filledTonalButtonColors()
            else ButtonDefaults.filledTonalButtonColors(
                containerColor = ToggleButtonDefaults.colors().containerColor,
                contentColor = ToggleButtonDefaults.colors().contentColor
            ),
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            if (date == null)
                showTimePicker = true
            else
                onChange(null)
        },
    ) {
        Icon(
            painter =
                if (date != null) painterResource(R.drawable.close)
                else painterResource(R.drawable.event_upcoming),
            modifier = Modifier.size(with(LocalDensity.current) {
                MaterialTheme.typography.bodyLarge.fontSize.toDp()
            }),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text =
                if (date != null) stringResource(
                    R.string.ui_main_alarms_edit_group_schedulebeginbutton_cancel,
                    LocalFormat.current.dateFormatter.format(date)
                )
                else stringResource(R.string.ui_main_alarms_edit_group_schedulebeginbutton_schedule),
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    if (showTimePicker)
        DatePicker(
            onSelect = {
                showTimePicker = false
                onChange(it)
            },
            onDismiss = {
                showTimePicker = false
            }
        )
}