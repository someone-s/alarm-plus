package com.eden.alarmplus.ui.main

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eden.alarmplus.ui.common.Format
import com.eden.alarmplus.ui.common.FormatViewModel
import com.eden.alarmplus.ui.common.FormatViewModelProvider
import com.eden.alarmplus.ui.common.LocalFormat
import com.eden.alarmplus.ui.main.navigation.rememberCommonBottomBarState
import com.eden.alarmplus.ui.main.navigation.AboutRoute
import com.eden.alarmplus.ui.main.about.AboutScreen
import com.eden.alarmplus.ui.main.navigation.AlarmsRoute
import com.eden.alarmplus.ui.main.alarms.GroupAlarmScreen
import com.eden.alarmplus.ui.main.navigation.topLevelRoutes


@Composable
fun MainContent(
    formatViewModel: FormatViewModel = viewModel(factory = FormatViewModelProvider.Factory),
) {
    val currentFormatUiState by formatViewModel.currentFormatUiState.collectAsState()
    val format = Format(timeFormatter = currentFormatUiState.timeFormatter)

    CompositionLocalProvider(LocalFormat provides format) {

        val navController = rememberNavController()

        val navBackStackEntry by navController.currentBackStackEntryAsState()

        val bottomBarState = rememberCommonBottomBarState(topLevelRoutes)

        LaunchedEffect(navBackStackEntry) {
            bottomBarState.currentDestination = navBackStackEntry?.destination
        }

        LaunchedEffect(bottomBarState.targetRoute) {
            if (bottomBarState.targetRoute == null) return@LaunchedEffect

            navController.navigate(bottomBarState.targetRoute!!.route) {

                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }

                launchSingleTop = true

                restoreState = true
            }

            bottomBarState.targetRoute = null
        }

        val sharedContentKey = "SCAFFOLD"

        SharedTransitionLayout {
            NavHost(navController, startDestination = AlarmsRoute) {
                composable<AlarmsRoute> { GroupAlarmScreen(
                    bottomBarState = bottomBarState,
                    sharedContentKey = sharedContentKey,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                ) }
                composable<AboutRoute> { AboutScreen(
                    bottomBarState = bottomBarState,
                    sharedContentKey = sharedContentKey,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this
                ) }
            }
        }
    }
}

