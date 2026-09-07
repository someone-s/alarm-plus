package com.eden.alarmplus.main.ui.alarms.edit.alarm

import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R

@Composable
fun BackButton(
    onClick: () -> Unit,
) {
    FilledIconButton(
        modifier = Modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_back),
            contentDescription = stringResource(R.string.main_ui_alarms_edit_alarm_backbutton_action)
        )
    }
}