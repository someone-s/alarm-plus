package com.eden.alarmplus.ui.main.alarms.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.state.StateType
import com.eden.alarmplus.ui.common.format.LocalFormat

object GroupItemDefaults {

    @Composable
    fun textStyles(
        titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
        firstTimeStyle: TextStyle = MaterialTheme.typography.displayMedium,
        remainingTimesStyle: TextStyle = MaterialTheme.typography.bodyMedium,
        dismissStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    ): GroupItemTextStyle {
        val textMeasurer = rememberTextMeasurer(cacheSize = 3)
        val density = LocalDensity.current

        return remember(titleStyle, firstTimeStyle, remainingTimesStyle, density) {
            GroupItemTextStyle(
                titleStyle = titleStyle,
                titleComputedHeight = with(density) { textMeasurer.measure("0", titleStyle).size.height.toDp() },
                firstTimeStyle = firstTimeStyle,
                firstTimeComputedHeight = with(density) { textMeasurer.measure("0", firstTimeStyle).size.height.toDp() },
                remainingTimesStyle = remainingTimesStyle,
                remainingTimesComputedHeight = with(density) { textMeasurer.measure("0", remainingTimesStyle).size.height.toDp() },
                dismissStyle = dismissStyle,
                dismissComputedHeight = with(density) { textMeasurer.measure("0", dismissStyle).size.height.toDp() },
            )
        }
    }
}

@Immutable
class GroupItemTextStyle(
    val titleStyle: TextStyle,
    val titleComputedHeight: Dp,
    val firstTimeStyle: TextStyle,
    val firstTimeComputedHeight: Dp,
    val remainingTimesStyle: TextStyle,
    val remainingTimesComputedHeight: Dp,
    val dismissStyle: TextStyle,
    val dismissComputedHeight: Dp,
)

private enum class GroupItemMode {
    DISMISSABLE,
    CURRENT,
    UNFOCUSED,
}

@Composable
fun GroupItem(
    group: GroupData,
    onSelect: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ListItemDefaults.segmentedShapes(0, 1),
    textStyles: GroupItemTextStyle = GroupItemDefaults.textStyles()
) {
    val mode = when (group.info.state) {
        StateType.DISABLED -> GroupItemMode.UNFOCUSED
        StateType.ENABLED_UNDEFINED_DEFAULT -> GroupItemMode.UNFOCUSED
        StateType.ENABLED_UNDEFINED_MANUAL -> GroupItemMode.UNFOCUSED
        StateType.ENABLED_PENDING -> GroupItemMode.UNFOCUSED
        StateType.ENABLED_PRIMED -> GroupItemMode.DISMISSABLE
        StateType.ENABLED_ACTIVE -> GroupItemMode.DISMISSABLE
        StateType.ENABLED_DISMISSED -> GroupItemMode.CURRENT
        StateType.ENABLED_IDLE -> GroupItemMode.CURRENT
    }

    val firstTime = group.alarms.firstOrNull()?.info?.time

    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val dismissBoxState = remember(group) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = positionalThreshold,
        )
    }

    val hapticFeedback = LocalHapticFeedback.current
    val timeFormatter = LocalFormat.current.timeFormatter


    var loaded by remember(group) { mutableStateOf(false) }
    LaunchedEffect(dismissBoxState.targetValue) {
        if (loaded)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        else
            loaded = true
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissBoxState,
        onDismiss = { onDelete() },
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
                        contentDescription = stringResource(R.string.ui_main_alarms_overview_groupitem_deleteaction),
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(
                                when (dismissBoxState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
                                }
                            ),
                    )
                }
            )
        },
        content = {
            Surface(
                shape = shapes.shape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.animateContentSize(),
            ) {
                Column {
                    SegmentedListItem(
                        shapes = shapes,
                        colors = when (mode) {
                            GroupItemMode.DISMISSABLE -> ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            GroupItemMode.CURRENT -> ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            GroupItemMode.UNFOCUSED -> ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onSelect()
                            }
                            .fillMaxWidth(),
                        content = {
                            Column {
                                Text(
                                    text = group.info.name ?: "",
                                    fontWeight = when (mode) {
                                        GroupItemMode.DISMISSABLE,
                                        GroupItemMode.CURRENT -> FontWeight.SemiBold
                                        GroupItemMode.UNFOCUSED -> FontWeight.Light
                                    },
                                    style = textStyles.titleStyle,
                                    maxLines = 1,
                                    modifier = Modifier.height(textStyles.titleComputedHeight),
                                )
                                Text(
                                    text = if (firstTime != null) timeFormatter.format(
                                        firstTime
                                    ) else "--:--",
                                    fontWeight = when (mode) {
                                        GroupItemMode.DISMISSABLE,
                                        GroupItemMode.CURRENT -> FontWeight.SemiBold
                                        GroupItemMode.UNFOCUSED -> FontWeight.Light
                                    },
                                    style = textStyles.firstTimeStyle,
                                    maxLines = 1,
                                    modifier = Modifier.height(textStyles.firstTimeComputedHeight),
                                )

                                val times = remember(group.alarms) {
                                    group.alarms.map { it.info.time }.take(5)
                                }

                                Row {
                                    Spacer(modifier = Modifier.height(textStyles.remainingTimesComputedHeight))
                                    times.forEach { remainingTime ->
                                        Text(
                                            text = timeFormatter.format(
                                                remainingTime
                                            ),
                                            fontWeight = when (mode) {
                                                GroupItemMode.DISMISSABLE,
                                                GroupItemMode.CURRENT -> FontWeight.SemiBold
                                                GroupItemMode.UNFOCUSED -> FontWeight.Normal
                                            },
                                            style = textStyles.remainingTimesStyle,
                                            maxLines = 1,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                            }
                        },
                        trailingContent = {
                            Switch(
                                checked = when (group.info.state) {
                                    StateType.DISABLED -> false
                                    StateType.ENABLED_UNDEFINED_DEFAULT,
                                    StateType.ENABLED_UNDEFINED_MANUAL,
                                    StateType.ENABLED_PENDING,
                                    StateType.ENABLED_PRIMED,
                                    StateType.ENABLED_ACTIVE,
                                    StateType.ENABLED_DISMISSED,
                                    StateType.ENABLED_IDLE -> true
                                },
                                onCheckedChange = {
                                    hapticFeedback.performHapticFeedback(
                                        if (it) HapticFeedbackType.ToggleOn
                                        else HapticFeedbackType.ToggleOff
                                    )
                                    onToggle(it)
                                },
                            )
                        },
                    )
                    AnimatedVisibility(
                        visible = mode == GroupItemMode.DISMISSABLE,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.End) {
                            ListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                    selectedContainerColor = Color.Transparent,
                                    draggedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    trailingContentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                content = {},
                                trailingContent = {
                                    Text(
                                        text = stringResource(R.string.ui_main_alarms_overview_groupitem_dismiss),
                                        fontWeight = when (mode) {
                                            GroupItemMode.DISMISSABLE,
                                            GroupItemMode.CURRENT -> FontWeight.SemiBold
                                            GroupItemMode.UNFOCUSED -> FontWeight.Normal
                                        },
                                        style = textStyles.dismissStyle,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .height(textStyles.dismissComputedHeight)
                                            .clickable(onClick = onDismiss),
                                    )
                                }
                            )
                        }
                    }
                }

            }
        }
    )
}