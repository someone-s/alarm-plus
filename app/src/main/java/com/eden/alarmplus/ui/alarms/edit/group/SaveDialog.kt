package com.eden.alarmplus.ui.alarms.edit.group

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R

@Composable
fun SaveDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_discarddialog_title)) },
        text = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_discarddialog_body)) },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_discarddialog_discardaction)) },
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_discarddialog_saveaction)) },
            )
        }
    )
}