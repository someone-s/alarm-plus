package com.eden.alarmplus.ui.common.components

import android.util.Log
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scrim
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun rememberSideSheetState(
    initialValue: SideSheetValue
) = remember(initialValue) {
    SideSheetState(initialValue)
}

enum class SideSheetValue { HIDDEN, EXPANDED }

@Stable
class SideSheetState(
    initialValue: SideSheetValue,
) {
    var panelWidth by mutableStateOf(480.dp)
    var targetValue by mutableStateOf(initialValue)
    var currentValue by mutableStateOf(SideSheetValue.HIDDEN)
    var offset = Animatable(1f)
    var dragHideThreshold by mutableFloatStateOf(0.3f)

    suspend fun show() {
        try {
            offset.animateTo(0f)
            currentValue = SideSheetValue.EXPANDED
        } catch (exception: CancellationException) {
            Log.i("$javaClass", exception.message, exception)
        }
    }

    suspend fun hide() {
        try {
            offset.animateTo(1f)
            currentValue = SideSheetValue.HIDDEN
        } catch (exception: CancellationException) {
            Log.i("$javaClass", exception.message, exception)
        }
    }
}

@Composable
fun ModalSideSheet(
    sheetState: SideSheetState,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    fun exit() = scope
        .launch { sheetState.hide() }
        .invokeOnCompletion { onDismissRequest() }

    val density = LocalDensity.current
    var dragOffset by remember { mutableFloatStateOf(sheetState.offset.value) }
    suspend fun onDragStarted() {
        dragOffset = sheetState.offset.value
        sheetState.offset.snapTo(dragOffset)
    }
    fun onDragDelta(delta: Float) {
        with(density) {
            dragOffset += delta.toDp().value / sheetState.panelWidth.value
            dragOffset = dragOffset.coerceIn(0f, 1f)
        }
        scope.launch {
            sheetState.offset.snapTo(dragOffset)
        }
    }
    suspend fun onDragStopped() {
        if (dragOffset < sheetState.dragHideThreshold) {
            dragOffset = 0f
            sheetState.offset.animateTo(dragOffset)
        }
        else {
            dragOffset = 1f
            exit()
        }
    }
    val draggableState = rememberDraggableState(onDelta = { onDragDelta(it) })

    LaunchedEffect(sheetState.targetValue) {
        if (sheetState.currentValue != sheetState.targetValue)
            when (sheetState.targetValue) {
                SideSheetValue.HIDDEN -> sheetState.hide()
                SideSheetValue.EXPANDED -> sheetState.show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStarted = { onDragStarted() },
                onDragStopped = { onDragStopped() }
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        Scrim(
            contentDescription = stringResource(R.string.ui_common_components_modalsidesheet_scrimaction),
            onClick = { exit() },
        )
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = sheetState.panelWidth)
                .offset {
                    IntOffset(
                        x = (sheetState.offset.value * sheetState.panelWidth.value).dp.toPx()
                            .toInt(),
                        y = 0
                    )
                },
            content = content,
        )
        PredictiveBackHandler { progress ->
            try {
                progress.collect { backEvent ->
                    // On gesture in-progress
                    sheetState.offset.snapTo(backEvent.progress)
                }

                // On gesture complete
                exit()

            } catch (_: CancellationException) {

                // On gesture canceled
                sheetState.show()
            }
        }
    }
}