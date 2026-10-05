package com.eden.alarmplus.ui.main.notification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R

@Composable
fun PermissionDialog(onDismiss: () -> Unit, onClick: () -> Unit) {

    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            dismissButton = {
                TextButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onDismiss()
                    },
                    content = { Text(stringResource(R.string.ui_main_notification_permissiondialog_ignoreaction)) }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onClick()
                    },
                    content = { Text(stringResource(R.string.ui_main_notification_permissiondialog_grantaction)) }
                )
            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.notification_important),
                    contentDescription = null,
                )
            },
            title = { Text(stringResource(R.string.ui_main_notification_permissiondialog_title)) },
            text = { Text(stringResource(R.string.ui_main_notification_permissiondialog_body, stringResource(R.string.app_name))) }
        )
    }
}