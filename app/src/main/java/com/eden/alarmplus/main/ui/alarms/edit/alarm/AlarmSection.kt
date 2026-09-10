package com.eden.alarmplus.main.ui.alarms.edit.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.AlarmData

interface Option

data class ExpandOption(
    val name: String,
    val value: String,
): Option

data class SwitchOption(
    val name: String,
    val value: Boolean,
): Option


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSection(
    alarm: AlarmData,
    onUpdate: (AlarmData) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {

    val options = listOf(
        ExpandOption(
            name = "A",
            value = "Val",
        ),
        SwitchOption(
            name = "B",
            value = false,
        ),
    )

    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {

        AlarmTime(
            alarm = alarm,
            onClick = { showTimePicker = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
        options.forEachIndexed { index, option ->
            AlarmOption(option, index, options)
        }
        Spacer(modifier = Modifier.height(16.dp))
        NavigateGroup(onSubmit, onCancel)
    }

    if (showTimePicker)
        AlarmTimePicker(
            alarm = alarm,
            onSelect = { time ->
                onUpdate(alarm.shallowCopy(time))
                showTimePicker = false
            },
            onDismiss = {
                showTimePicker = false
            },
        )
}

