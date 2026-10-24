package com.eden.alarmplus.ui.main.repeats.edit.group

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

    val hapticFeedback = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            OutlinedTextField(
                state = textFieldState,
                label = { Text(text = stringResource(R.string.ui_main_repeats_edit_group_titleinput_title)) }
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onDismiss()
                },
                content = { Text(text = stringResource(R.string.ui_main_repeats_edit_group_titleinput_cancelaction)) },
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    val newTitle = textFieldState.text
                    onSubmit(if (newTitle.isEmpty()) null else newTitle.toString())
                },
                content = { Text(text = stringResource(R.string.ui_main_repeats_edit_group_titleinput_submitaction)) },
            )
        }
    )
}