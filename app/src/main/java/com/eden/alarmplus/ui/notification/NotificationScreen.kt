package com.eden.alarmplus.ui.notification

import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import java.time.LocalTime

enum class WindowMode {
    LANDSCAPE, SQUARE, PORTRAIT
}

@Composable
fun NotificationScreen(
    title: String?,
    time: LocalTime?,
    onSnooze: (() -> Unit)?,
    onDismiss: (() -> Unit)?,
) {

    val windowInfo = LocalWindowInfo.current
    val windowMode by remember(windowInfo) { derivedStateOf {
        if (windowInfo.containerSize.height == 0) return@derivedStateOf WindowMode.SQUARE

        val aspectRatio = windowInfo.containerSize.width.toFloat() / windowInfo.containerSize.height.toFloat()

        if (aspectRatio > 1.2f) WindowMode.LANDSCAPE
        else if (aspectRatio > 0.8f) WindowMode.SQUARE
        else WindowMode.PORTRAIT
    } }

    Scaffold { innerPadding ->
        FlexBox(
            config = {
                direction(when (windowMode) {
                    WindowMode.LANDSCAPE -> FlexDirection.Row
                    WindowMode.SQUARE,
                    WindowMode.PORTRAIT -> FlexDirection.Column
                })
                alignItems(FlexAlignItems.Stretch)
                justifyContent(FlexJustifyContent.SpaceEvenly)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            InfoSection(
                modifier = Modifier
                    .fillMaxHeight()
                    .flex { basis(0.75f) },
                title,
                time,
                windowInfo.containerDpSize,
                windowMode,
            )
            ButtonGroup(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        when (windowMode) {
                            WindowMode.LANDSCAPE -> it.flex { basis(0.5f) }
                            WindowMode.SQUARE,
                            WindowMode.PORTRAIT -> it.height(128.dp)
                        }
                    },
                windowMode = windowMode,
                onSnooze = onSnooze,
                onDismiss = onDismiss
            )
        }
    }
}

@PreviewScreenSizes
@Composable
fun NotificationScreenPreview() {

    AlarmPlusTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationScreen(
                title = "Wake up Early to 134 efmesinfsdj",
                time = LocalTime.of(13, 5),
                onSnooze = null,
                onDismiss = {},
            )
        }
    }
}