package com.eden.alarmplus.ui.main.repeats.edit.alarm

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R

@Composable
fun DeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.ui_main_repeats_edit_alarm_deletedialog_title)) },
        text = { Text(text = stringResource(R.string.ui_main_repeats_edit_alarm_deletedialog_body)) },
        dismissButton = {
            TextButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onDismiss()
                },
                content = { Text(text = stringResource(R.string.ui_main_repeats_edit_alarm_deletedialog_cancelaction)) },
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onConfirm()
                },
                content = { Text(text = stringResource(R.string.ui_main_repeats_edit_alarm_deletedialog_deleteaction)) },
            )
        }
    )
}