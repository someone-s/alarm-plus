package com.eden.alarmplus.ui.common.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max

data class CompanionAlignments(
    val startToEndAlignment: Alignment = Alignment.CenterStart,
    val endToStartAlignment: Alignment = Alignment.CenterEnd,
    val settledAlignment: Alignment = Alignment.Center,
)

object CompanionSwipeToDismissBoxDefaults {
    private val defaultCompanionGap: Dp = 2.dp
    fun companionGap(): Dp = defaultCompanionGap

    private val defaultCompanionAlignments: CompanionAlignments = CompanionAlignments()
    fun companionAlignments(): CompanionAlignments = defaultCompanionAlignments
    fun companionAlignments(
        startToEndAlignment: Alignment = defaultCompanionAlignments.startToEndAlignment,
        endToStartAlignment: Alignment = defaultCompanionAlignments.endToStartAlignment,
        settledAlignment: Alignment = defaultCompanionAlignments.settledAlignment,
    ): CompanionAlignments = CompanionAlignments(
        startToEndAlignment = startToEndAlignment,
        endToStartAlignment = endToStartAlignment,
        settledAlignment = settledAlignment,
    )
}

@Composable
fun CompanionSwipeToDismissBox(
    state: SwipeToDismissBoxState,
    companionContent: @Composable (
        modifier: Modifier,
        alignment: Alignment,
    ) -> Unit,
    companionGap: Dp = CompanionSwipeToDismissBoxDefaults.companionGap(),
    companionAlignment: CompanionAlignments = CompanionSwipeToDismissBoxDefaults.companionAlignments(),
    modifier: Modifier,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    gesturesEnabled: Boolean = true,
    onDismiss: (SwipeToDismissBoxValue) -> Unit = {},
    content: @Composable RowScope.() -> Unit,
) {
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val alignment = when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> companionAlignment.startToEndAlignment
                SwipeToDismissBoxValue.EndToStart -> companionAlignment.endToStartAlignment
                SwipeToDismissBoxValue.Settled -> companionAlignment.settledAlignment
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = alignment,
                content = {
                    companionContent(
                        Modifier.layout { measurable, constraints ->
                            val offset = try {
                                state.requireOffset().toInt()
                            } catch (exception: IllegalStateException) {
                                Log.i("$javaClass", "Offset not initialized.", exception)
                                0
                            }
                            val gap = companionGap.toPx().toInt()
                            val width = max(0, abs(offset) - gap)
                            val placeable = measurable.measure(
                                constraints.copy(minWidth = width, maxWidth = width)
                            )
                            layout(width = placeable.width, height = placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        },
                        alignment
                    )
                }
            )
        },
        modifier = modifier,
        enableDismissFromStartToEnd = enableDismissFromStartToEnd,
        enableDismissFromEndToStart = enableDismissFromEndToStart,
        gesturesEnabled = gesturesEnabled,
        onDismiss = onDismiss,
        content = content
    )
}