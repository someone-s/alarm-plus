package com.eden.alarmplus.main.ui.alarms.edit.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.alarmplus.R
@Composable
fun NavigateGroup(
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        OutlinedButton(
            onClick = onDelete,
            content = { Text(text = stringResource(R.string.main_ui_alarms_edit_group_navigategroup_deleteaction)) }
        )
        FilledIconButton(
            onClick = onBack,
            content = {
                Icon(
                    painter = painterResource(R.drawable.arrow_downward),
                    contentDescription = stringResource(R.string.main_ui_alarms_edit_group_navigategroup_applyaction),
                )
            },
        )
    }
}