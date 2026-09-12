package com.eden.alarmplus.main.ui.alarms.edit.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R
@Composable
fun NavigateGroup(
    hasChanges: Boolean,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        FilledIconButton(
            modifier = Modifier,
            onClick = onCancel,
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = stringResource(R.string.main_ui_alarms_edit_alarm_navigategroup_backaction)
            )
        }
        FilledTonalButton(
            enabled = hasChanges,
            onClick = onSubmit,
            content = { Text(text = stringResource(R.string.main_ui_alarms_edit_alarm_navigategroup_submitaction)) },
        )
    }
}