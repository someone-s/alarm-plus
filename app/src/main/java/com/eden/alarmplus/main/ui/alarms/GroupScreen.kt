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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateMap
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
import com.eden.alarmplus.main.ui.alarms.overview.AddButton
import com.eden.alarmplus.main.ui.alarms.edit.EditSheet
import com.eden.alarmplus.main.ui.alarms.edit.rememberEditSheetState
import com.eden.alarmplus.main.ui.alarms.overview.GroupItem
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
object AlarmsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen() {

    val groups = remember {
        listOf(
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
            .map { Pair(it.info.uuid, it) }
            .toMutableStateMap()
    }

    val editSheetState = rememberEditSheetState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        floatingActionButton = {
            AddButton(
                onClick = {
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
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
                    items(groups.values.toList()) { group ->
                        GroupItem(
                            group = group,
                            onSelectGroup = { selectedGroup ->
                                editSheetState.showSheet(selectedGroup)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item { Spacer(Modifier.height(64.dp)) }
                }
            }

            val revertGroupDeleteMessage =
                stringResource(R.string.main_ui_alarms_groupscreen_revertgroupdeletemessage)
            val revertGroupDeleteAction =
                stringResource(R.string.main_ui_alarms_groupscreen_revertgroupdeleteaction)
            val revertGroupUpdateMessage =
                stringResource(R.string.main_ui_alarms_groupscreen_revertgroupupdatemessage)
            val revertGroupUpdateAction =
                stringResource(R.string.main_ui_alarms_groupscreen_revertgroupupdateaction)
            EditSheet(
                state = editSheetState,
                onDelete = {
                    val deleteGroup = editSheetState.group
                    groups.remove(deleteGroup.info.uuid)
                    scope.launch {
                        val result = snackbarHostState
                            .showSnackbar(
                                message = revertGroupDeleteMessage,
                                actionLabel = revertGroupDeleteAction,
                                duration = SnackbarDuration.Long,
                                withDismissAction = true,
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                groups[deleteGroup.info.uuid] = deleteGroup
                            }
                            SnackbarResult.Dismissed -> {}
                        }
                    }
                },
                onBack = {
                    if (editSheetState.modified) {
                        val editGroup = editSheetState.group
                        val groupUuid = editGroup.info.uuid
                        val originalGroup = groups[groupUuid]
                        groups[groupUuid] = editGroup

                        scope.launch {
                            val result = snackbarHostState
                                .showSnackbar(
                                    message = revertGroupUpdateMessage,
                                    actionLabel = revertGroupUpdateAction,
                                    duration = SnackbarDuration.Long,
                                    withDismissAction = true,
                                )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    if (originalGroup != null)
                                        groups[groupUuid] = originalGroup
                                    else
                                        groups.remove(groupUuid)
                                }
                                SnackbarResult.Dismissed -> {}
                            }
                        }
                    }
                }
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
                    GroupScreen()
                }
            }
        }
    }
}