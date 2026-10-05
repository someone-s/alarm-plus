package com.eden.alarmplus.ui.main.alarms.overview

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R


@Composable
fun AddButton(
    onClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    FloatingActionButton(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        },
    ) {
        Icon(
            painter = painterResource(R.drawable.add),
            contentDescription = stringResource(R.string.ui_main_alarms_addbutton_text),
        )
    }
}