package com.eden.alarmplus.main.ui.alarms.edit.group

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R


@Composable
fun AddButton(
    shapes: ListItemShapes,
) {
    SegmentedListItem(
        shapes = shapes,
        content = {
            Text(
                text = stringResource(R.string.main_ui_alarms_edit_group_addbutton_text),
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = stringResource(R.string.main_ui_alarms_edit_group_addbutton_action),
            )
        }
    )
}