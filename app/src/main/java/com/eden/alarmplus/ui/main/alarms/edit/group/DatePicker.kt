package com.eden.alarmplus.ui.main.alarms.edit.group

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun DatePicker(
    onSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    val hapticFeedback = LocalHapticFeedback.current

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null)
                        onSelect(Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneOffset.UTC).toLocalDate())
                    else
                        onDismiss()
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                content = { Text(text = stringResource(R.string.ui_main_alarms_edit_group_datepicker_applyaction)) },
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onDismiss()
                },
                content = { Text(text = stringResource(R.string.ui_main_alarms_edit_group_datepicker_cancelaction)) },
            )
        },
        content = { DatePicker(state = datePickerState) }
    )
}