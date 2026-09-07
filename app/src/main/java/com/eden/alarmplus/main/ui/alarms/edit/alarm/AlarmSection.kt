package com.eden.alarmplus.main.ui.alarms.edit.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eden.alarmplus.data.AlarmData

interface Option

data class ExpandOption(
    val name: String,
    val value: String,
): Option

data class SwitchOption(
    val name: String,
    val value: Boolean,
): Option


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSection(
    alarm: AlarmData,
    onBack: () -> Unit,
) {

    val options = listOf(
        ExpandOption(
            name = "A",
            value = "Val",
        ),
        SwitchOption(
            name = "B",
            value = false,
        ),
    )
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {

        AlarmTime(alarm)
        Spacer(modifier = Modifier.height(24.dp))
        options.forEachIndexed { index, option ->
            AlarmOption(option, index, options)
        }
        Spacer(modifier = Modifier.height(16.dp))
        BackButton(
            onClick = onBack,
        )
    }
}

