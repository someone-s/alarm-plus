package com.eden.alarmplus.ui.main.repeats.edit.group

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.feature.DayWeekOfMonthFlags

@Composable
fun DayWeekOfMonthButton(
    modifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.squareShape,
    flags: DayWeekOfMonthFlags,
    onUpdate: (DayWeekOfMonthFlags, Boolean) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    val notSpecified = flags.all(DayWeekOfMonthFlags.ALL)

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
                    flags.split()
                        .let { it
                            .withIndex()
                            .map { (index, flag) ->
                                val flagText = stringResource(
                                    when(flag) {
                                        DayWeekOfMonthFlags.FIRST       -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_first
                                        DayWeekOfMonthFlags.SECOND      -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_second
                                        DayWeekOfMonthFlags.THIRD       -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_third
                                        DayWeekOfMonthFlags.FORTH       -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_forth
                                        DayWeekOfMonthFlags.LAST        -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_last
                                        DayWeekOfMonthFlags.PENULTIMATE -> R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_penultimate
                                        else -> throw IllegalArgumentException("DayWeekOfMonthFlags out of range")
                                    }
                                )
                                stringResource(
                                    if (index == 0)
                                        R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_seperatorstart
                                    else if (index == it.lastIndex)
                                        R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_seperatorend
                                    else
                                        R.string.ui_main_repeats_edit_group_dayweekofmonthbutton_description_seperator,
                                    flagText
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
}