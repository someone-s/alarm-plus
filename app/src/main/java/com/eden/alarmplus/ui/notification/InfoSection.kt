package com.eden.alarmplus.ui.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.R
import com.eden.alarmplus.ui.common.LocalFormat
import java.time.LocalTime

@Composable
fun InfoSection(
    modifier: Modifier = Modifier,
    title: String?,
    time: LocalTime?,
    windowSize: DpSize,
    windowMode: WindowMode,
) {
    val hourFormatter = LocalFormat.current.largeHourFormatter
    val minuteFormatter = LocalFormat.current.largeMinuteFormatter

    with (LocalDensity.current) {
        Column(
            modifier = modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (windowMode) {
                WindowMode.LANDSCAPE -> {
                    Text(
                        text =
                            if (time != null) "${hourFormatter.format(time)}:${minuteFormatter.format(time)}"
                            else "--:--",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = (windowSize.height * 0.25f).toSp(),
                        lineHeight = (windowSize.height * 0.25f).toSp(),
                        textAlign = TextAlign.End,
                    )
                    Text(
                        text = title
                            ?: stringResource(R.string.ui_notification_infosection_placeholder),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = (windowSize.height * 0.10f).toSp(),
                        lineHeight = (windowSize.height * 0.10f).toSp(),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                WindowMode.SQUARE -> {
                    Text(
                        text =
                            if (time != null) "${hourFormatter.format(time)}\n${minuteFormatter.format(time)}"
                            else "--\n--",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = (windowSize.width * 0.30f).toSp(),
                        lineHeight = (windowSize.width * 0.25f).toSp(),
                    )
                    Text(
                        text = title
                            ?: stringResource(R.string.ui_notification_infosection_placeholder),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = (windowSize.width * 0.10f).toSp(),
                        lineHeight = (windowSize.width * 0.10f).toSp(),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                WindowMode.PORTRAIT -> {
                    Text(
                        text =
                            if (time != null) "${hourFormatter.format(time)}\n${minuteFormatter.format(time)}"
                            else "--\n--",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = (windowSize.width * 0.40f).toSp(),
                        lineHeight = (windowSize.width * 0.35f).toSp(),
                    )
                    Text(
                        text = title
                            ?: stringResource(R.string.ui_notification_infosection_placeholder),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = (windowSize.width * 0.10f).toSp(),
                        lineHeight = (windowSize.width * 0.10f).toSp(),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}