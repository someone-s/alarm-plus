package com.eden.alarmplus.ui.main.alarms.edit.alarm

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource

@Stable
open class Option(
    val icon: Int,
    val name: String,
    val onInteract: () -> Unit,
)

@Stable
class ExpandOption(
    icon: Int,
    name: String,
    val value: String,
    onInteract: () -> Unit,
): Option(
    icon = icon,
    name = name,
    onInteract = onInteract,
)

@Stable
class SwitchOption(
    icon: Int,
    name: String,
    val value: Boolean,
    onInteract: () -> Unit,
): Option(
    icon = icon,
    name = name,
    onInteract = onInteract,
)

@Composable
fun AlarmOption(
    shapes: ListItemShapes,
    option: Option,
) {
    val hapticFeedback = LocalHapticFeedback.current

    when (option) {
        is ExpandOption ->
            SegmentedListItem(
                modifier = Modifier.clickable {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    option.onInteract()
                },
                shapes = shapes,
                leadingContent = {
                    Icon(
                        painter = painterResource(option.icon),
                        contentDescription = null
                    )
                },
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
                leadingContent = {
                    Icon(
                        painter = painterResource(option.icon),
                        contentDescription = null
                    )
                },
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
                        onCheckedChange = {
                            hapticFeedback.performHapticFeedback(
                                if (it) HapticFeedbackType.ToggleOn
                                else HapticFeedbackType.ToggleOff
                            )
                            option.onInteract()
                        },
                    )
                },
            )
    }
}