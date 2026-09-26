package com.eden.alarmplus.ui.alarms.edit.alarm

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

open class Option(
    val name: String,
    val onInteract: () -> Unit,
)

class ExpandOption(
    name: String,
    val value: String,
    onInteract: () -> Unit,
): Option(
    name = name,
    onInteract = onInteract,
)

class SwitchOption(
    name: String,
    val value: Boolean,
    onInteract: () -> Unit,
): Option(
    name = name,
    onInteract = onInteract,
)

@Composable
fun AlarmOption(
    shapes: ListItemShapes,
    option: Option,
) {
    when (option) {
        is ExpandOption ->
            SegmentedListItem(
                modifier = Modifier.clickable { option.onInteract() },
                shapes = shapes,
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
                shapes = shapes,
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
                        onCheckedChange = { option.onInteract() },
                    )
                },
            )
    }
}