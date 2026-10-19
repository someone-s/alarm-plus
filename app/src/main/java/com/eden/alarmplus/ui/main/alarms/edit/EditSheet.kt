package com.eden.alarmplus.ui.main.alarms.edit

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.eden.alarmplus.data.alarms.structure.alarm.getUniqueAlarmData
import com.eden.alarmplus.data.alarms.structure.dayofweek.DayOfWeekFlags
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.GroupInfo
import com.eden.alarmplus.data.alarms.structure.group.emptyGroupData
import com.eden.alarmplus.data.alarms.structure.group.feature.DayOfWeekFeature
import com.eden.alarmplus.data.alarms.structure.group.getUniqueGroupData
import com.eden.alarmplus.ui.common.components.ModalSideSheet
import com.eden.alarmplus.ui.common.components.SideSheetValue
import com.eden.alarmplus.ui.common.components.rememberSideSheetState
import kotlinx.coroutines.launch

@Composable
fun rememberEditSheetState(
    initialShowSheet: Boolean = false,
    initialGroup: GroupData = getUniqueGroupData(),
): EditSheetState {
    return remember { EditSheetState(
        initialShowSheet = initialShowSheet,
        initialGroup = initialGroup,
    ) }
}
@Stable
class EditSheetState(
    initialShowSheet: Boolean = false,
    initialGroup: GroupData = getUniqueGroupData(),
) {
    var showSheet by mutableStateOf(initialShowSheet)
    var group by mutableStateOf(initialGroup)
    var modified by mutableStateOf(false)

    fun showSheet(
        group: GroupData,
        initialModified: Boolean = false,
    ) {
        this.group = group
        this.showSheet = true
        this.modified = initialModified
    }

    fun modifyGroup(
        group: GroupData,
    ) {
        this.group = group
        this.modified = true
    }

    fun resetGroup() {
        this.group = emptyGroupData
        this.modified = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(
    state: EditSheetState,
    windowInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()


    val editContentState = rememberEditContentState(state.group)

    val hapticFeedback = LocalHapticFeedback.current

    val useSideSheet = windowInfo.windowSizeClass.isAtLeastBreakpoint(
        widthDpBreakpoint = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
        heightDpBreakpoint = WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    )

    if (useSideSheet) {
        val sideState = rememberSideSheetState(
            if (state.showSheet) SideSheetValue.EXPANDED
            else SideSheetValue.HIDDEN
        )

        fun hideSheet() {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            coroutineScope.launch {
                sideState.hide()
                state.showSheet = false
                state.resetGroup()
            }
        }

        if (state.showSheet)
            ModalSideSheet(
                sheetState = sideState,
                onDismissRequest = {
                    if (editContentState.modified)
                        state.modifyGroup(editContentState.group)
                    onBack()
                    hideSheet()
                }
            ) {
                EditContent(
                    editContentState = editContentState,
                    maximise = true,
                    onDelete = {
                        if (editContentState.modified)
                            state.modifyGroup(editContentState.group)
                        onDelete()
                        hideSheet()
                    },
                    onBack = {
                        if (editContentState.modified)
                            state.modifyGroup(editContentState.group)
                        onBack()
                        hideSheet()
                    }
                )
            }
    }
    else {
        val bottomState = rememberBottomSheetState(
            if (state.showSheet) SheetValue.Expanded
            else SheetValue.Hidden
        )

        fun hideSheet() {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            coroutineScope.launch {
                bottomState.hide()
                state.showSheet = false
                state.resetGroup()
            }
        }

        if (state.showSheet)
            ModalBottomSheet(
                modifier = Modifier.widthIn(max = 480.dp),
                onDismissRequest = {
                    if (editContentState.modified)
                        state.modifyGroup(editContentState.group)
                    onBack()
                    hideSheet()
                },
                sheetState = bottomState,
            ) {
                EditContent(
                    editContentState = editContentState,
                    maximise = false,
                    onDelete = {
                        if (editContentState.modified)
                            state.modifyGroup(editContentState.group)
                        onDelete()
                        hideSheet()
                    },
                    onBack = {
                        if (editContentState.modified)
                            state.modifyGroup(editContentState.group)
                        onBack()
                        hideSheet()
                    }
                )
            }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@PreviewScreenSizes
@Composable
fun EditSheetPreview() {

    val editSheetState = rememberEditSheetState(
        initialShowSheet = true,
        initialGroup = GroupData(
            info = GroupInfo(
                name = "Morning Alarm",
                features = listOf(
                    DayOfWeekFeature(DayOfWeekFlags.merge(
                        DayOfWeekFlags.WEDNESDAY,
                        DayOfWeekFlags.FRIDAY
                    ))
                )
            ),
            alarms = listOf(
                getUniqueAlarmData(),
                getUniqueAlarmData(),
            ),
        )
    )

    Scaffold {
        EditSheet(
            state = editSheetState,
            onDelete = {},
            onBack = {},
        )
    }
}
