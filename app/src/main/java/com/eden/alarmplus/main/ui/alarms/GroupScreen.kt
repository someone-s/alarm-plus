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
import com.eden.alarmplus.data.alarm.AlarmData
import com.eden.alarmplus.data.alarm.AlarmInfo
import com.eden.alarmplus.data.group.GroupData
import com.eden.alarmplus.data.group.GroupInfo
import com.eden.alarmplus.data.group.emptyGroupData
import com.eden.alarmplus.main.ui.alarms.edit.EditMode
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
fun GroupScreen(
    initialSheetVisible: Boolean = false,
    initialEditMode: EditMode = EditMode.LIST,
    initialShowDiscardDialog: Boolean = false,
) {

    val groups = remember {
        mutableListOf(
            GroupData(
                info = GroupInfo(
                    name = "Morning alarms",
                    active = true,
                ),
                alarms = listOf(
                    AlarmData(
                        info = AlarmInfo(
                            time = LocalTime.of(8, 30),
                        ),
                        features = emptyList(),
                    ),
                    AlarmData(
                        info = AlarmInfo(
                            time = LocalTime.of(9, 30),
                        ),
                        features = emptyList(),
                    ),
                    AlarmData(
                        info = AlarmInfo(
                            time = LocalTime.of(10, 30),
                        ),
                        features = emptyList(),
                    ),
                )
            ),
            GroupData(
                info = GroupInfo(
                    name = "Lunch alerts",
                    active = false,
                ),
                alarms = listOf(
                    AlarmData(
                        info = AlarmInfo(
                            time = LocalTime.of(12, 30),
                        ),
                        features = emptyList(),
                    ),
                    AlarmData(
                        info = AlarmInfo(
                            time = LocalTime.of(13, 30),
                        ),
                        features = emptyList(),
                    ),
                )
            ),
        )
    }

    var showEditSheet by remember { mutableStateOf(initialSheetVisible) }
    val editSheetState = rememberBottomSheetState(initialValue = if (initialSheetVisible) SheetValue.Expanded else SheetValue.Hidden)
    var selectedGroup by remember { mutableStateOf(emptyGroupData) }

    Scaffold(
        floatingActionButton = {
            AddButton(
                onClick = {}
            )
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
                    onUpdate = { },
                    onCancel = { showEditSheet = false },
                    initialEditMode = initialEditMode,
                    initialShowDiscardDialog = initialShowDiscardDialog,
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
                composable<AlarmsRoute> {
                    GroupScreen(
                        initialSheetVisible = true,
                        initialEditMode = EditMode.ITEM,
                        initialShowDiscardDialog = true,
                    )
                }
            }
        }
    }
}