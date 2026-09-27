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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import com.eden.alarmplus.ui.theme.PolygonShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationContent(
    onSnooze: (() -> Unit)?,
    onDismiss: (() -> Unit)?,
) {

    val windowInfo = LocalWindowInfo.current
    val isLandscape by remember(windowInfo) { derivedStateOf {
        windowInfo.containerSize.width > windowInfo.containerSize.height
    } }

    val dismissClip = remember { PolygonShape(MaterialShapes.Cookie4Sided) }

    Scaffold { innerPadding ->
        FlexBox(
            config = {
                direction(if (isLandscape) FlexDirection.Row else FlexDirection.Column)
                alignItems(FlexAlignItems.Stretch)
                justifyContent(FlexJustifyContent.SpaceEvenly)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .let {
                    if (isLandscape) it.padding(horizontal = 16.dp)
                    else it.padding(horizontal = 24.dp)
                }
        ) {
            FlexBox(
                config = {
                    direction(FlexDirection.Column)
                    alignItems(FlexAlignItems.Center)
                    justifyContent(FlexJustifyContent.SpaceEvenly)
                    gap(4.dp)
                },
                modifier = Modifier
                    .let {
                        if (isLandscape) it
                            .fillMaxHeight()
                            .flex { basis(0.75f) }
                        else it.fillMaxWidth()
                    }
                    .let {
                        if (isLandscape) it
                        else it.height(128.dp)
                    },
            ) {
                Text("Placeholder")
            }
            FlexBox(
                config = {
                    direction(if (isLandscape) FlexDirection.Column else FlexDirection.Row)
                    alignItems(FlexAlignItems.Stretch)
                    justifyContent(FlexJustifyContent.SpaceEvenly)
                    gap(4.dp)
                },
                modifier = Modifier
                    .let {
                        if (isLandscape) it
                            .fillMaxHeight()
                            .flex { basis(0.5f) }
                        else it.fillMaxWidth()
                    }
                    .let {
                        if (isLandscape) it
                        else it.height(128.dp)
                    },
            ) {

                FilledTonalButton(
                    onClick = { if (onSnooze != null) onSnooze() },
                    enabled = onSnooze != null,
                    modifier = Modifier
                        .flex { basis(1f) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                ) {
                    Text(stringResource(R.string.ui_notification_notificationcontent_snoozeaction))
                }

                if (onDismiss != null)
                    FilledTonalButton(
                        onClick = onDismiss,
                        shape = RectangleShape,
                        modifier = Modifier
                            .flex {
                                basis(128.dp)
                                grow(0f)
                                shrink(0f)
                            }
                            .clip(dismissClip),
                    ) {
                        Text(stringResource(R.string.ui_notification_notificationcontent_dismissaction))
                    }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun NotificationContentPreview() {

    AlarmPlusTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationContent(
                onSnooze = null,
                onDismiss = {},
            )
        }
    }
}