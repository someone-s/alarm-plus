package com.eden.alarmplus.ui.main.alarms

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.alarmplus.R
import com.eden.alarmplus.data.alarms.structure.group.GroupData
import com.eden.alarmplus.data.alarms.structure.group.getUniqueGroupData
import com.eden.alarmplus.ui.main.navigation.CommonRouteState
import com.eden.alarmplus.ui.main.alarms.overview.AddButton
import com.eden.alarmplus.ui.main.alarms.edit.EditSheet
import com.eden.alarmplus.ui.main.alarms.edit.rememberEditSheetState
import com.eden.alarmplus.ui.main.alarms.overview.GroupItem
import com.eden.alarmplus.ui.main.navigation.AlarmsRoute
import com.eden.alarmplus.ui.main.navigation.populateScope
import com.eden.alarmplus.ui.main.navigation.topLevelRoutes
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import kotlinx.coroutines.launch

@Composable
fun GroupAlarmScreen(
    viewModel: GroupAlarmViewModel = viewModel(factory = GroupAlarmViewModelProvider.Factory),
    sharedContentKey: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomBarState: CommonRouteState,
) = with(sharedTransitionScope) {

    val groupAlarmUiState by viewModel.groupAlarmsUiState.collectAsState()

    val editSheetState = rememberEditSheetState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val hapticFeedback = LocalHapticFeedback.current

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
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
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
        val originalGroup = groupAlarmUiState.groupList.firstOrNull { it.info.uuid == groupUuid }

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
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
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

    NavigationSuiteScaffold(
        navigationSuiteItems = { bottomBarState.populateScope(this, onBeforeNavigate = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        }) },
        modifier = Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(sharedContentKey),
            animatedVisibilityScope = animatedVisibilityScope
        ),
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { GroupAlarmTopBar(scrollBehavior) },
            floatingActionButton = { AddButton(
                onClick = {
                    editSheetState.showSheet(
                        group = getUniqueGroupData(),
                        initialModified = true
                    )
                }
            ) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState,) },
            content = { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 72.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,

                        ) {
                        items(
                            items = groupAlarmUiState.groupList,
                            key = { it.info.uuid }
                        ) { group ->

                            GroupItem(
                                modifier = Modifier
                                    .widthIn(max = 400.dp)
                                    .animateItem(),
                                group = group,
                                onSelect = {
                                    editSheetState.showSheet(group)
                                },
                                onToggle = { enabled ->
                                    val groupUuid = group.info.uuid
                                    scope.launch {
                                        viewModel.toggleGroupAlarm(groupUuid, enabled)
                                    }
                                },
                                onDelete = {
                                    showRevertGroupDeleteSnackbar(group)
                                },
                                onDismiss = {
                                    val groupUuid = group.info.uuid
                                    scope.launch {
                                        viewModel.dismissGroupAlarm(groupUuid)
                                    }
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

            }
        )

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

}


@PreviewScreenSizes
@Composable
fun GroupAlarmScreenPreview() {

    val navController = rememberNavController()

    AlarmPlusTheme {
        SharedTransitionLayout {
            NavHost(navController, startDestination = AlarmsRoute) {
                composable<AlarmsRoute> {
                    GroupAlarmScreen(
                        viewModel = mockGroupAlarmViewModel,
                        sharedContentKey = "SCAFFOLD",
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        bottomBarState = CommonRouteState(topLevelRoutes),
                    )
                }
            }
        }
    }
}