package com.eden.alarmplus.ui.main.alarms.edit.alarm

import android.annotation.SuppressLint
import android.media.RingtoneManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmData
import com.eden.alarmplus.data.alarms.structure.alarm.AlarmInfo
import com.eden.alarmplus.data.alarms.structure.alarm.feature.AudioFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.PinStartFeature
import com.eden.alarmplus.data.alarms.structure.alarm.feature.VibrateFeature
import com.eden.alarmplus.data.alarms.structure.alarm.getUniqueAlarmData
import com.eden.alarmplus.data.alarms.structure.audio.AudioData
import java.time.LocalTime


@Composable
fun rememberAlarmSectionState(key: Any): AlarmSectionState {
    return remember(key) { AlarmSectionState() }
}
@Stable
class AlarmSectionState {
    var value by mutableStateOf(getUniqueAlarmData())
    var modified by mutableStateOf(false)

    fun setAlarmDraft(alarm: AlarmData) {
        value = alarm
        modified = true
    }

    fun setAlarmCurrent(alarm: AlarmData) {
        value = alarm
        modified = false
    }

    fun clearAlarm() {
        value = getUniqueAlarmData()
        modified = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSection(
    state: AlarmSectionState,
    maximise: Boolean = false,
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
            icon = R.drawable.volume_up,
            name = stringResource(R.string.ui_main_alarms_edit_alarm_alarmsection_option_sound),
            value = state.value.getFeature<AudioFeature>()?.data?.name ?: "Unset",
            onInteract = {
                ringtonePickerLauncher.launch(state.value.getFeature<AudioFeature>()?.data?.uri)
            },
        ),
        SwitchOption(
            icon = R.drawable.mobile_vibrate,
            name = stringResource(R.string.ui_main_alarms_edit_alarm_alarmsection_option_vibrate),
            value = state.value.getFeature<VibrateFeature>() != null,
            onInteract = {
                if (state.value.getFeature<VibrateFeature>() != null)
                    state.value = state.value.shallowCopyRemoveFeature<VibrateFeature>()
                else
                    state.value = state.value.shallowCopyAddFeature(VibrateFeature())
                state.modified = true
            },
        ),
        SwitchOption(
            icon = R.drawable.keep,
            name = stringResource(R.string.ui_main_alarms_edit_alarm_alarmsection_option_pinstart),
            value = state.value.getFeature<PinStartFeature>() != null,
            onInteract = {
                if (state.value.getFeature<PinStartFeature>() != null)
                    state.value = state.value.shallowCopyRemoveFeature<PinStartFeature>()
                else
                    state.value = state.value.shallowCopyAddFeature(PinStartFeature())
                state.modified = true
            },
        ),
    )

    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .let {
                if (maximise) it.fillMaxSize()
                else it
            }
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        LazyColumn(
            modifier = Modifier
                .let {
                    if (maximise) it.weight(1f)
                    else it
                },
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                AlarmTime(
                    alarm = state.value,
                    onClick = { showTimePicker = true }
                )
            }
            item {
                Spacer(modifier = Modifier.height(22.dp))
            }
            itemsIndexed(options) { index, option ->
                Spacer(modifier = Modifier.height(2.dp))
                AlarmOption(
                    shapes = ListItemDefaults.segmentedShapes(
                        index,
                        options.size,
                    ),
                    option = option,
                )
            }
            if (!maximise) item {
                Spacer(modifier = Modifier.height(16.dp))
                NavigateGroup(
                    onBack = onBack,
                    onDelete = {
                        showDeleteDialog = true
                    },
                )
            }
        }
        if (maximise) {
            Spacer(modifier = Modifier.height(16.dp))
            NavigateGroup(
                onBack = onBack,
                onDelete = {
                    showDeleteDialog = true
                },
            )
        }
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



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(widthDp = 500, heightDp = 300, showSystemUi = true)
@Composable
fun GroupSectionPreview() {
    val sheetState = rememberBottomSheetState(SheetValue.Expanded)
    val alarmSectionState = remember {
        val s = AlarmSectionState()
        s.value = AlarmData(
            info = AlarmInfo(
                time = LocalTime.of(12, 53)
            ),
            features = listOf(
                VibrateFeature()
            )
        )
        s
    }


    Scaffold {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {},
        ) {
            AlarmSection(
                state = alarmSectionState,
                onDelete = {},
                onBack = {},
            )
        }
    }
}