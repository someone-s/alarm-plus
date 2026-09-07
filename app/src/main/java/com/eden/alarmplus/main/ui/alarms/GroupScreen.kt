package com.eden.alarmplus.main.ui.alarms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.alarmplus.R
import com.eden.alarmplus.data.AlarmData
import com.eden.alarmplus.data.AlarmStatus
import com.eden.alarmplus.data.GroupData
import com.eden.alarmplus.data.GroupStatus
import com.eden.alarmplus.data.emptyGroupData
import com.eden.alarmplus.main.ui.alarms.overview.AddButton
import com.eden.alarmplus.main.ui.alarms.edit.EditSheet
import com.eden.alarmplus.main.ui.alarms.overview.GroupItem
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
object AlarmsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen() {

    val groups = listOf(
        GroupData(
            name = "Morning alarms",
            status = GroupStatus.ENABLED,
            alarms = listOf(
                AlarmData(
                    time = LocalTime.of(8, 30),
                    status = AlarmStatus.IN_PROGRESS,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(9, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(9, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
            )
        ),
        GroupData(
            name = "Lunch alerts",
            status = GroupStatus.DISABLED,
            alarms = listOf(
                AlarmData(
                    time = LocalTime.of(12, 30),
                    status = AlarmStatus.IN_PROGRESS,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(12, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
            )
        ),
        GroupData(
            name = "Morning alarms",
            status = GroupStatus.ENABLED,
            alarms = listOf(
                AlarmData(
                    time = LocalTime.of(8, 30),
                    status = AlarmStatus.IN_PROGRESS,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(9, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(9, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
            )
        ),
        GroupData(
            name = "Lunch alerts",
            status = GroupStatus.DISABLED,
            alarms = listOf(
                AlarmData(
                    time = LocalTime.of(12, 30),
                    status = AlarmStatus.IN_PROGRESS,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(12, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
            )
        ),
        GroupData(
            name = "Morning alarms",
            status = GroupStatus.ENABLED,
            alarms = listOf(
                AlarmData(
                    time = LocalTime.of(8, 30),
                    status = AlarmStatus.IN_PROGRESS,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(9, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(9, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
            )
        ),
        GroupData(
            name = "Lunch alerts",
            status = GroupStatus.DISABLED,
            alarms = listOf(
                AlarmData(
                    time = LocalTime.of(12, 30),
                    status = AlarmStatus.IN_PROGRESS,
                    features = emptyList(),
                ),
                AlarmData(
                    time = LocalTime.of(12, 30),
                    status = AlarmStatus.PENDING,
                    features = emptyList(),
                ),
            )
        ),
    )

    var showEditSheet by remember { mutableStateOf(false) }
    val editSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    var selectedGroup by remember { mutableStateOf(emptyGroupData) }

    Scaffold(
        floatingActionButton = {
            AddButton()
        },
        // inset is already handled by parent Scaffold
        contentWindowInsets = WindowInsets(),
        content = { paddingValues ->

            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
                Spacer(Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        painter = painterResource(R.drawable.alarm),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.main_ui_alarms_alarmscreen_title),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(20.dp))
                }
                Spacer(Modifier.height(32.dp))

                LazyColumn(
                    modifier = Modifier
                        .width(480.dp),
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(groups) { group ->
                        GroupItem(
                            group = group,
                            onSelectGroup = {
                                selectedGroup = it
                                showEditSheet = true
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item { Spacer(Modifier.height(64.dp)) }
                }
            }

            if (showEditSheet)
                EditSheet(
                    sheetState = editSheetState,
                    group = selectedGroup,
                    onDismiss = { showEditSheet = false }
                )
        }
    )
}


@PreviewScreenSizes
@Composable
fun GroupScreenPreview() {

    val navController = rememberNavController()

    AlarmPlusTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {}
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = AlarmsRoute, Modifier.padding(innerPadding)) {
                composable<AlarmsRoute> { GroupScreen() }
            }
        }
    }
}