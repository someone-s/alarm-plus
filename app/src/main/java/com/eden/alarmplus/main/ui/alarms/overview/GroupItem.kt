package com.eden.alarmplus.main.ui.alarms.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.group.GroupData
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun GroupItem(
    group: GroupData,
    onSelectGroup: (GroupData) -> Unit,
) {
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    val firstTime = group.alarms.firstOrNull()?.info?.time

    SegmentedListItem(
        modifier = Modifier.clickable { onSelectGroup(group) },
        shapes = ListItemDefaults.segmentedShapes(0, 1),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Column {
                Text(
                    text = group.info.name ?: "",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (firstTime != null) timeFormatter.format(firstTime) else "--:--",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.displayMedium,
                )
                Row {
                    group.alarms
                        .map { it.info.time }
                        .forEach { remainingTime ->
                            Text(
                                text = timeFormatter.format(remainingTime),
                                fontWeight = FontWeight.Normal,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                }
            }
        },
        trailingContent = {
            Switch(
                checked = group.info.active,
                onCheckedChange = {},
            )
        },
    )
}