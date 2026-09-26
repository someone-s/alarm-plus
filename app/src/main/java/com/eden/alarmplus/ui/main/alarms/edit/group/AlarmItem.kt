package com.eden.alarmplus.ui.main.alarms.edit.group

import android.text.format.DateFormat
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.structure.alarm.AlarmData
import com.eden.alarmplus.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.structure.alarm.feature.VibrateFeature
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AlarmItem(
    alarm: AlarmData,
    onSelect: (AlarmData) -> Unit,
    onDelete: (AlarmData) -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {

    val context = LocalContext.current
    val timeFormatter = remember(DateFormat.is24HourFormat(context)) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }

    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val dismissBoxState = remember(alarm) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = positionalThreshold,
        )
    }

    SwipeToDismissBox(
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
                        contentDescription = stringResource(R.string.main_ui_alarms_edit_group_alarmitem_deleteaction),
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
                onClick = { onSelect(alarm) },
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

                        if (alarm.getFeature<AudioFeature>() != null)
                            Icon(
                                painter = painterResource(R.drawable.volume_up),
                                contentDescription = stringResource(R.string.main_ui_alarms_edit_group_alarmitem_feature_audible),
                            )
                        else if (alarm.getFeature<VibrateFeature>() != null)
                            Icon(
                                painter = painterResource(R.drawable.mobile_vibrate),
                                contentDescription = stringResource(R.string.main_ui_alarms_edit_group_alarmitem_feature_vibrate),
                            )
                        else
                            Icon(
                                painter = painterResource(R.drawable.volume_off),
                                contentDescription = stringResource(R.string.main_ui_alarms_edit_group_alarmitem_feature_silent),
                            )
                        VerticalDivider()
                        Icon(
                            painter = painterResource(R.drawable.chevron_forward),
                            contentDescription = stringResource(R.string.main_ui_alarms_edit_group_alarmitem_feature_silent),
                        )
                    }
                }
            )
        }
    )
}