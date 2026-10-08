package com.eden.alarmplus.ui.main.alarms.edit.alarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.ui.common.LocalFormat
import com.eden.alarmplus.ui.theme.PolygonShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmTime(
    alarm: AlarmData,
    onClick: () -> Unit,
) {
    val clip = remember { PolygonShape(MaterialShapes.Cookie6Sided) }

    val hapticFeedback = LocalHapticFeedback.current
    val timeFormatter = LocalFormat.current.timeFormatter

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = timeFormatter.format(alarm.info.time),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.displaySmall,
            maxLines = 1,
            modifier = Modifier.clickable(onClick = onClick),
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            modifier = Modifier
                .size(56.dp)
                .clip(clip),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            shape = RectangleShape,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = stringResource(R.string.ui_main_alarms_edit_alarm_alarmtime_action),
            )
        }
    }
}