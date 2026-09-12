package com.eden.alarmplus.main.ui.alarms.edit.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.emptyAlarmData

interface Option

data class ExpandOption(
    val name: String,
    val value: String,
): Option

data class SwitchOption(
    val name: String,
    val value: Boolean,
): Option

@Composable
fun rememberAlarmSectionState(): AlarmSectionState {
    return remember { AlarmSectionState() }
}
@Stable
class AlarmSectionState {
    var value by mutableStateOf(emptyAlarmData)
    var modified by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSection(
    state: AlarmSectionState,
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
            alarm = state.value,
            onClick = { showTimePicker = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
        options.forEachIndexed { index, option ->
            AlarmOption(option, index, options)
        }
        Spacer(modifier = Modifier.height(16.dp))
        NavigateGroup(
            onSubmit = onSubmit,
            onCancel = onCancel
        )
    }

    if (showTimePicker)
        AlarmTimePicker(
            alarm = state.value,
            onSelect = { time ->
                state.value = state.value.shallowCopy(time)
                state.modified = true
                showTimePicker = false
            },
            onDismiss = {
                showTimePicker = false
            },
        )
}

