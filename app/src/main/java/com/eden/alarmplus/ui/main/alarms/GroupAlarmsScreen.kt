package com.eden.alarmplus.ui.main.alarms

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.getUniqueGroupData
import com.eden.alarmplus.ui.main.alarms.overview.AddButton
import com.eden.alarmplus.ui.main.alarms.edit.EditSheet
import com.eden.alarmplus.ui.main.alarms.edit.rememberEditSheetState
import com.eden.alarmplus.ui.main.alarms.overview.GroupItem
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object AlarmsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAlarmScreen(
    viewModel: GroupAlarmViewModel = viewModel(factory = GroupAlarmViewModelProvider.Factory)
) {

    val groupAlarmUiState by viewModel.groupAlarmsUiState.collectAsState()

    val editSheetState = rememberEditSheetState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val revertGroupDeleteMessage = stringResource(R.string.ui_main_alarms_groupscreen_revertgroupdeletemessage)
    val revertGroupDeleteAction = stringResource(R.string.ui_main_alarms_groupscreen_revertgroupdeleteaction)
    fun showRevertGroupDeleteSnackbar(deleteGroup: GroupData) {
        scope.launch {
            viewModel.deleteGroupAlarm(deleteGroup.info.uuid)

            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState
                .showSnackbar(
                    message = revertGroupDeleteMessage,
                    actionLabel = revertGroupDeleteAction,
                    duration = SnackbarDuration.Long,
                    withDismissAction = true,
                )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewModel.insertOrReplaceGroupAlarm(deleteGroup)
                }

                SnackbarResult.Dismissed -> {}
            }
        }
    }

    val revertGroupUpdateMessage = stringResource(R.string.ui_main_alarms_groupscreen_revertgroupupdatemessage)
    val revertGroupUpdateAction = stringResource(R.string.ui_main_alarms_groupscreen_revertgroupupdateaction)
    fun showRevertGroupUpdateSnackbar(editGroup: GroupData) {
        val groupUuid = editGroup.info.uuid
        val originalGroup = groupAlarmUiState.groupDataList.firstOrNull { it.info.uuid == groupUuid }

        scope.launch {
            viewModel.insertOrReplaceGroupAlarm(editGroup)
            
            snackbarHostState.currentSnackbarData?.dismiss()
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
                        viewModel.insertOrReplaceGroupAlarm(originalGroup)
                    else
                        viewModel.deleteGroupAlarm(groupUuid)
                }

                SnackbarResult.Dismissed -> {}
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            AddButton(
                onClick = {
                    editSheetState.showSheet(
                        group = getUniqueGroupData(),
                        initialModified = true,
                    )
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
                        text = stringResource(R.string.ui_main_alarms_alarmscreen_title),
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
                    items(items = groupAlarmUiState.groupDataList) { group ->
                        GroupItem(
                            group = group,
                            onSelect = {
                                editSheetState.showSheet(group)
                            },
                            onToggle = { active ->
                                val groupUuid = group.info.uuid
                                scope.launch {
                                    viewModel.toggleGroupAlarm(groupUuid, active)
                                }
                            },
                            onDelete = {
                                showRevertGroupDeleteSnackbar(group)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item { Spacer(Modifier.height(64.dp)) }
                }
            }

            EditSheet(
                state = editSheetState,
                onDelete = {
                    showRevertGroupDeleteSnackbar(editSheetState.group)
                },
                onBack = {
                    if (editSheetState.modified) {
                        val editGroup = editSheetState.group
                        showRevertGroupUpdateSnackbar(editGroup)
                    }
                }
            )
        }
    )
}


@PreviewScreenSizes
@Composable
fun GroupAlarmScreenPreview() {

    val navController = rememberNavController()

    AlarmPlusTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {}
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = AlarmsRoute, Modifier.padding(innerPadding)) {
                composable<AlarmsRoute> {
                    GroupAlarmScreen()
                }
            }
        }
    }
}