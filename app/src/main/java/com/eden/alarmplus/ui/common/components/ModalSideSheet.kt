package com.eden.alarmplus.ui.common.components

import android.util.Log
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
    var targetValue by mutableStateOf(initialValue)
    var currentValue by mutableStateOf(SideSheetValue.HIDDEN)
    var offset = Animatable(1f)

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

    LaunchedEffect(sheetState.targetValue) {
        if (sheetState.currentValue != sheetState.targetValue)
            when (sheetState.targetValue) {
                SideSheetValue.HIDDEN -> sheetState.hide()
                SideSheetValue.EXPANDED -> sheetState.show()
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Scrim(
            contentDescription = "Close Sheet",
            onClick = { exit() },
        )
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 480.dp)
                .offset { IntOffset(x = (sheetState.offset.value * 480).dp.toPx().toInt(), y = 0) },
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