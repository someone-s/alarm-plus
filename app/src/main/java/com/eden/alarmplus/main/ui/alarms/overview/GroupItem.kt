package com.eden.alarmplus.main.ui.alarms.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.GroupData
import com.eden.alarmplus.data.GroupStatus
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun GroupItem(
    group: GroupData,
    onSelectGroup: (GroupData) -> Unit,
) {
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    val firstTime = group.alarms.firstOrNull()?.time
    val remainingTimes =
        if (group.alarms.size >= 2)
            group.alarms.subList(1, group.alarms.size).map { it.time }
        else
            emptyList()

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
                    text = group.name ?: "",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (firstTime != null) timeFormatter.format(firstTime) else "--:--",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = remainingTimes.joinToString(", ") { timeFormatter.format(it) },
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        },
        trailingContent = {
            Switch(
                checked = when (group.status) {
                    GroupStatus.DISABLED -> false
                    GroupStatus.ENABLED -> true
                },
                onCheckedChange = {},
            )
        },
    )
}