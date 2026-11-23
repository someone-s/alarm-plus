package com.eden.alarmplus.ui.main.repeats.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekFromEndOfMonthFlags
import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekFromStartOfMonthFlags

private val startFlagsToResource = mapOf(
    DayWeekFromStartOfMonthFlags.FIRST  to R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_first,
    DayWeekFromStartOfMonthFlags.SECOND to R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_second,
    DayWeekFromStartOfMonthFlags.THIRD  to R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_third,
    DayWeekFromStartOfMonthFlags.FORTH  to R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_forth,
)

private val endFlagsToResource = mapOf(
    DayWeekFromEndOfMonthFlags.LAST        to R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_last,
    DayWeekFromEndOfMonthFlags.PENULTIMATE to R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_penultimate,
)

@Composable
fun DayWeekOfMonthButton(
    modifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.squareShape,
    startFlags: DayWeekFromStartOfMonthFlags,
    endFlags: DayWeekFromEndOfMonthFlags,
    onUpdateFlags: (DayWeekFromStartOfMonthFlags, DayWeekFromEndOfMonthFlags) -> Unit,
) {

    var showDialog by remember { mutableStateOf(false) }
    
    var mutableStartFlags by remember(showDialog, startFlags, endFlags) {
        if (
            startFlags.all(DayWeekFromStartOfMonthFlags.ALL) &&
            endFlags.all(DayWeekFromEndOfMonthFlags.ALL)
        )
            mutableStateOf(DayWeekFromStartOfMonthFlags.NONE)
        else
            mutableStateOf(startFlags)

    }
    var mutableEndFlags by remember(showDialog, startFlags, endFlags) {
        if (
            startFlags.all(DayWeekFromStartOfMonthFlags.ALL) &&
            endFlags.all(DayWeekFromEndOfMonthFlags.ALL)
        )
            mutableStateOf(DayWeekFromEndOfMonthFlags.NONE)
        else
            mutableStateOf(endFlags)
    }

    val hapticFeedback = LocalHapticFeedback.current

    val notSpecified = startFlags.all(DayWeekFromStartOfMonthFlags.ALL) && endFlags.all(DayWeekFromEndOfMonthFlags.ALL)
    val displayTextSet = remember(startFlags, endFlags) {
        startFlags.split().map { flag -> startFlagsToResource[flag]!! } +
        endFlags.split().map { flag -> endFlagsToResource[flag]!! }
    }

    FilledTonalButton(
        modifier = modifier,
        shape = shape,
        colors =
            if (notSpecified) ButtonDefaults.filledTonalButtonColors(
                containerColor = ToggleButtonDefaults.colors().containerColor,
                contentColor = ToggleButtonDefaults.colors().contentColor
            )
            else ButtonDefaults.filledTonalButtonColors(),
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            showDialog = true
        },
    ) {
        Icon(
            painter = painterResource(R.drawable.date_range),
            modifier = Modifier.size(with(LocalDensity.current) {
                MaterialTheme.typography.bodyLarge.fontSize.toDp()
            }),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(
                R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description,
                if (notSpecified) stringResource(
                    R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_all)
                else stringResource(
                    R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_combined,
                    displayTextSet.let { it
                        .withIndex()
                        .map { (index, flagText) ->
                            stringResource(
                                when (index) {
                                    0 -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_seperatorstart
                                    it.lastIndex -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_seperatorend
                                    else -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_seperator
                                },
                                stringResource(flagText)
                            )
                        }
                    }
                    .joinToString(separator = "")
                )
            ),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis
        )
    }

    if (showDialog)
        Dialog(
            onDismissRequest = {
                showDialog = false
            }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp)
            ) {
                val startFlags = DayWeekFromStartOfMonthFlags.ALL.split()
                val endFlags = DayWeekFromEndOfMonthFlags.ALL.split()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_dialog_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_dialog_fromstartlabel),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        startFlags.forEachIndexed { index, flag ->
                            val checked = mutableStartFlags.any(flag)
                            ToggleButton(
                                modifier = Modifier.weight(1f),
                                shapes = when(index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    startFlags.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                                checked = checked,
                                onCheckedChange = { enabled ->
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    mutableStartFlags =
                                        if (enabled) DayWeekFromStartOfMonthFlags.merge(mutableStartFlags, flag)
                                        else mutableStartFlags.copyClear(flag)
                                },
                                content = {
                                    Text(text = stringResource(startFlagsToResource[flag]!!))
                                },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_dialog_fromendlabel),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        endFlags.forEachIndexed { index, flag ->
                            val checked = mutableEndFlags.any(flag)
                            ToggleButton(
                                modifier = Modifier.weight(1f),
                                shapes = when(index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    endFlags.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                                checked = checked,
                                onCheckedChange = { enabled ->
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    mutableEndFlags =
                                        if (enabled) DayWeekFromEndOfMonthFlags.merge(mutableEndFlags, flag)
                                        else mutableEndFlags.copyClear(flag)
                                },
                                content = {
                                    Text(text = stringResource(endFlagsToResource[flag]!!))
                                },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                showDialog = false
                            },
                            content = { Text(text = stringResource(R.string.ui_main_repeats_edit_group_titleinput_cancelaction)) },
                        )
                        FilledTonalButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onUpdateFlags(mutableStartFlags, mutableEndFlags)
                                showDialog = false
                            },
                            content = {
                                Text(text = stringResource(R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_dialog_save))
                            }
                        )
                    }
                }
            }
        }
}