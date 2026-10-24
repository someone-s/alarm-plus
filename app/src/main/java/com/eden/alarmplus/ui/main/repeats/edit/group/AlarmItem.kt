package com.eden.alarmplus.ui.main.repeats.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.PinStartFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.VibrateFeature
import com.eden.alarmplus.ui.common.format.LocalFormat

@Composable
fun AlarmItem(
    alarm: AlarmData,
    onSelect: (AlarmData) -> Unit,
    onDelete: (AlarmData) -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val dismissBoxState = remember(alarm) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = positionalThreshold,
        )
    }

    val hapticFeedback = LocalHapticFeedback.current
    val timeFormatter = LocalFormat.current.timeFormatter


    var loaded by remember(alarm) { mutableStateOf(false) }
    LaunchedEffect(dismissBoxState.targetValue) {
        if (loaded)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        else
            loaded = true
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissBoxState,
        onDismiss = { onDelete(alarm) },
        backgroundContent = {
            SegmentedListItem(
                shapes = shapes,
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                ),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = stringResource(R.string.ui_main_repeats_edit_group_alarmitem_deleteaction),
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(when (dismissBoxState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
                            }),
                    )
                }
            )
        },
        content = {
            SegmentedListItem(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onSelect(alarm)
                },
                shapes = shapes,
                content = {
                    Text(
                        text = timeFormatter.format(alarm.info.time),
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        val pinStartFeature = alarm.getFeature<PinStartFeature>()
                        if (pinStartFeature != null)
                            Icon(
                                painter = painterResource(R.drawable.keep),
                                contentDescription = stringResource(R.string.ui_main_repeats_edit_group_alarmitem_feature_pinstart),
                            )

                        val audioFeature = alarm.getFeature<AudioFeature>()
                        if (audioFeature?.data != null)
                            Icon(
                                painter = painterResource(R.drawable.volume_up),
                                contentDescription = stringResource(R.string.ui_main_repeats_edit_group_alarmitem_feature_audible),
                            )
                        else if (alarm.getFeature<VibrateFeature>() != null)
                            Icon(
                                painter = painterResource(R.drawable.mobile_vibrate),
                                contentDescription = stringResource(R.string.ui_main_repeats_edit_group_alarmitem_feature_vibrate),
                            )
                        else
                            Icon(
                                painter = painterResource(R.drawable.volume_off),
                                contentDescription = stringResource(R.string.ui_main_repeats_edit_group_alarmitem_feature_silent),
                            )

                        VerticalDivider()
                        Icon(
                            painter = painterResource(R.drawable.chevron_forward),
                            contentDescription = stringResource(R.string.ui_main_repeats_edit_group_alarmitem_feature_silent),
                        )
                    }
                }
            )
        }
    )
}