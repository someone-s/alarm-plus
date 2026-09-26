package com.eden.alarmplus.ui.main.alarms.edit.alarm

import android.media.RingtoneManager
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.structure.alarm.AlarmData
import com.eden.alarmplus.data.structure.alarm.emptyAlarmData
import com.eden.alarmplus.data.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.structure.alarm.feature.VibrateFeature
import com.eden.alarmplus.data.structure.audio.AudioData


@Composable
fun rememberAlarmSectionState(): AlarmSectionState {
    return remember { AlarmSectionState() }
}
@Stable
class AlarmSectionState {
    var value by mutableStateOf(emptyAlarmData)
    var modified by mutableStateOf(false)

    fun setAlarmDraft(alarm: AlarmData) {
        value = alarm
        modified = true
    }

    fun setAlarmCurrent(alarm: AlarmData) {
        value = alarm
        modified = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSection(
    state: AlarmSectionState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = RingtonePickerContract(),
        onResult = { result ->
            state.value = state.value.shallowCopyReplaceFeature(
                AudioFeature(
                    data =
                        if (result != null)
                            AudioData(
                                name = RingtoneManager
                                    .getRingtone(context, result)
                                    .getTitle(context),
                                uri = result,
                            )
                        else
                            null
                )
            )
            state.modified = true
        }
    )

    val options = listOf(
        ExpandOption(
            name = "Sound",
            value = state.value.getFeature<AudioFeature>()?.data?.name ?: "Unset",
            onInteract = {
                ringtonePickerLauncher.launch(state.value.getFeature<AudioFeature>()?.data?.uri)
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
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
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
            onBack = onBack,
            onDelete = {
                showDeleteDialog = true
            },
        )
    }

    if (showTimePicker)
        TimePicker(
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

    if (showDeleteDialog)
        DeleteDialog(
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = {
                showDeleteDialog = false
            },
        )
}

