package com.eden.alarmplus.main.ui.alarms.edit.alarm

import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AlarmOption(
    option: Option,
    index: Int,
    options: List<Option>
) {
    when (option) {
        is ExpandOption ->
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(
                    index,
                    options.size,
                ),
                content = {
                    Text(
                        text = option.name,
                        maxLines = 1,
                    )
                },
                supportingContent = {
                    Text(
                        text = option.value,
                        maxLines = 1,
                    )
                },
            )

        is SwitchOption ->
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(
                    index,
                    options.size,
                ),
                content = {
                    Text(
                        text = option.name,
                        maxLines = 1,
                    )
                },
                supportingContent = { },
                trailingContent = {
                    Switch(
                        checked = option.value,
                        onCheckedChange = null,
                    )
                },
            )
    }
}