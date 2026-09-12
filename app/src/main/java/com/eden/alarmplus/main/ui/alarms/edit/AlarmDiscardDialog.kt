package com.eden.alarmplus.main.ui.alarms.edit

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R

@Composable
fun AlarmDiscardDialog(
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.main_ui_alarms_edit_alarmdiscarddialog_title)) },
        text = { Text(text = stringResource(R.string.main_ui_alarms_edit_alarmdiscarddialog_body)) },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_alarmdiscarddialog_discardaction)) },
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onSubmit,
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_alarmdiscarddialog_saveaction)) },
            )
        }
    )
}