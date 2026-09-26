package com.eden.alarmplus.ui.main.alarms.edit.alarm

import android.text.format.DateFormat
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.structure.alarm.AlarmData
import com.eden.alarmplus.ui.theme.PolygonShape
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmTime(
    alarm: AlarmData,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val timeFormatter = remember(DateFormat.is24HourFormat(context)) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }

    val clip = remember { PolygonShape(MaterialShapes.Cookie6Sided) }

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
            onClick = onClick,
        ) {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = stringResource(R.string.main_ui_alarms_edit_alarm_alarmtime_action),
            )
        }
    }
}