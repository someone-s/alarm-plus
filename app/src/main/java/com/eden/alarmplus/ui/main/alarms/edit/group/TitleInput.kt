package com.eden.alarmplus.ui.main.alarms.edit.group

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R

@Composable
fun TitleInput(
    title: String?,
    onSubmit: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val textFieldState = rememberTextFieldState(
        initialText = title ?: "",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            OutlinedTextField(
                state = textFieldState,
                label = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_titleinput_title)) }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_titleinput_cancelaction)) },
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    val newTitle = textFieldState.text
                    onSubmit(if (newTitle.isEmpty()) null else newTitle.toString())
                },
                content = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_titleinput_submitaction)) },
            )
        }
    )
}