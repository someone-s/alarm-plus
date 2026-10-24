package com.eden.alarmplus.ui.main.repeats.edit.alarm

import android.text.format.DateFormat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.isPm
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import java.time.LocalTime

@Composable
fun TimePicker(
    alarm: AlarmData,
    onSelect: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val timePickerState = rememberTimePickerState(
        initialHour = alarm.info.time.hour,
        initialMinute = alarm.info.time.minute,
        is24Hour = DateFormat.is24HourFormat(context),
    )

    val hapticFeedback = LocalHapticFeedback.current


    var dialLoaded by remember(alarm) { mutableStateOf(false) }
    LaunchedEffect(timePickerState.hourInput, timePickerState.minuteInput) {
        if (dialLoaded)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        else
            dialLoaded = true
    }


    var selectionLoaded by remember(alarm) { mutableStateOf(false) }
    LaunchedEffect(timePickerState.selection) {
        if (selectionLoaded)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        else
            selectionLoaded = true
    }

    var zoneLoaded by remember(alarm) { mutableStateOf(false) }
    LaunchedEffect(timePickerState.isPm) {
        if (zoneLoaded)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        else
            zoneLoaded = true
    }

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = {},
        content = { TimePicker(timePickerState) },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onSelect(LocalTime.of(timePickerState.hour, timePickerState.minute))
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                content = { Text(text = stringResource(R.string.ui_main_repeats_edit_alarm_alarmtimepicker_applyaction)) },
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onDismiss()
                },
                content = { Text(text = stringResource(R.string.ui_main_repeats_edit_alarm_alarmtimepicker_cancelaction)) },
            )
        }
    )
}