package com.eden.alarmplus.ui.notification

import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.ui.theme.PolygonShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ButtonGroup(
    modifier: Modifier = Modifier,
    windowMode: WindowMode,
    onSnooze: (() -> Unit)?,
    onDismiss: (() -> Unit)?,
) {

    val hapticFeedback = LocalHapticFeedback.current
    val dismissClip = remember { PolygonShape(MaterialShapes.Cookie4Sided) }

    FlexBox(
        config = {
            direction(
                when (windowMode) {
                    WindowMode.LANDSCAPE -> FlexDirection.Column
                    WindowMode.SQUARE,
                    WindowMode.PORTRAIT -> FlexDirection.Row
                }
            )
            alignItems(FlexAlignItems.Stretch)
            justifyContent(FlexJustifyContent.SpaceEvenly)
            gap(4.dp)
        },
        modifier = modifier,
    ) {

        FilledTonalButton(
            onClick = {
                if (onSnooze != null) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onSnooze()
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                }
            },
            enabled = onSnooze != null,
            shape = when (windowMode) {
                WindowMode.LANDSCAPE -> RoundedCornerShape(CornerSize(64.dp))
                WindowMode.SQUARE,
                WindowMode.PORTRAIT -> ButtonDefaults.shape
            },
            modifier = Modifier
                .flex { basis(1f) },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        ) {
            Text(
                text = stringResource(R.string.ui_notification_notificationcontent_snoozeaction),
                style = when (windowMode) {
                    WindowMode.LANDSCAPE -> MaterialTheme.typography.displayMedium
                    WindowMode.SQUARE,
                    WindowMode.PORTRAIT -> MaterialTheme.typography.titleLarge
                },
            )
        }

        if (onDismiss != null)
            FilledTonalButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onDismiss()
                },
                shape = when (windowMode) {
                    WindowMode.LANDSCAPE -> RoundedCornerShape(CornerSize(64.dp))
                    WindowMode.SQUARE,
                    WindowMode.PORTRAIT -> RectangleShape
                },
                modifier = Modifier
                    .flex {
                        basis(128.dp)
                        grow(0f)
                        shrink(0f)
                    }
                    .let {
                        when (windowMode) {
                            WindowMode.LANDSCAPE -> it
                            WindowMode.SQUARE,
                            WindowMode.PORTRAIT -> it.clip(dismissClip)
                        }
                    },
            ) {
                Text(
                    text = stringResource(R.string.ui_notification_notificationcontent_dismissaction),
                    style = when (windowMode) {
                        WindowMode.LANDSCAPE -> MaterialTheme.typography.displayMedium
                        WindowMode.SQUARE,
                        WindowMode.PORTRAIT -> MaterialTheme.typography.titleLarge
                    },
                )
            }
    }
}