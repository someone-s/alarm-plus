package com.eden.alarmplus.main.ui.alarms.edit.alarm

import android.text.format.DateFormat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R
import com.eden.alarmplus.data.AlarmData
import java.time.LocalTime

@Composable
fun AlarmTimePicker(
    alarm: AlarmData,
    onSelect: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val timePickerState = rememberTimePickerState(
        initialHour = alarm.time.hour,
        initialMinute = alarm.time.minute,
        is24Hour = DateFormat.is24HourFormat(context),
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = {},
        content = { TimePicker(timePickerState) },
        confirmButton = {
            FilledTonalButton(
                onClick = { onSelect(LocalTime.of(timePickerState.hour, timePickerState.minute)) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_alarm_alarmtimepicker_applyaction)) },
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_alarm_alarmtimepicker_cancelaction)) },
            )
        }
    )
}