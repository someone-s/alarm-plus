package com.eden.alarmplus.main.ui.alarms.edit.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.alarm.emptyAlarmData
import com.eden.alarmplus.data.alarm.feature.AudioFeature
import com.eden.alarmplus.data.alarm.feature.VibrateFeature


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
            name = "Sound",
            value = state.value.getFeature<AudioFeature>()?.data?.name ?: "Unset",
            onInteract = {
            },
        ),
        SwitchOption(
            name = "Vibrate",
            value = state.value.getFeature<VibrateFeature>() != null,
            onInteract = {
                if (state.value.getFeature<VibrateFeature>() != null)
                    state.value = state.value.shallowCopyRemoveFeature<VibrateFeature>()
                else
                    state.value = state.value.shallowCopyAddFeature(VibrateFeature())
                state.modified = true
            },
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
            AlarmOption(
                shapes = ListItemDefaults.segmentedShapes(
                    index,
                    options.size,
                ),
                option = option,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        NavigateGroup(
            hasChanges = state.modified,
            onSubmit = onSubmit,
            onCancel = onCancel,
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

