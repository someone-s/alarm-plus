package com.eden.alarmplus.main.ui.alarms.edit.group

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
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
import com.eden.alarmplus.data.alarm.AlarmData
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AlarmItem(
    shape: ListItemShapes,
    alarm: AlarmData,
    onClick: () -> Unit,
) {

    val context = LocalContext.current
    val timeFormatter = remember(DateFormat.is24HourFormat(context)) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }

    SegmentedListItem(
        onClick = onClick,
        shapes = shape,
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
                Icon(
                    painter = painterResource(R.drawable.mobile_vibrate),
                    contentDescription = stringResource(R.string.main_ui_alarms_edit_group_alarmitem_feature_vibrate),
                )
                Icon(
                    painter = painterResource(R.drawable.volume_up),
                    contentDescription = stringResource(R.string.main_ui_alarms_edit_group_alarmitem_feature_audible),
                )
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