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
import com.eden.alarmplus.ui.common.format.DateFormatViewModel
import com.eden.alarmplus.ui.common.format.DateFormatViewModelProvider
import com.eden.alarmplus.ui.common.format.Format
import com.eden.alarmplus.ui.common.format.TimeFormatViewModel
import com.eden.alarmplus.ui.common.format.TimeFormatViewModelProvider
import com.eden.alarmplus.ui.common.format.LocalFormat
import com.eden.alarmplus.ui.main.navigation.rememberRouteState
import com.eden.alarmplus.ui.main.navigation.AboutRoute
import com.eden.alarmplus.ui.main.about.AboutScreen
import com.eden.alarmplus.ui.main.navigation.RepeatsRoute
import com.eden.alarmplus.ui.main.repeats.RepeatsScreen
import com.eden.alarmplus.ui.main.navigation.topLevelRoutes


@Composable
fun MainContent(
    dateFormatViewModel: DateFormatViewModel = viewModel(factory = DateFormatViewModelProvider.Factory),
    timeFormatViewModel: TimeFormatViewModel = viewModel(factory = TimeFormatViewModelProvider.Factory),
) {
    val currentDateFormatUiState by dateFormatViewModel.currentDateFormatUiState.collectAsState()
    val currentTimeFormatUiState by timeFormatViewModel.currentTimeFormatUiState.collectAsState()
    val format = Format(
        dateFormatter = currentDateFormatUiState.dateFormatter,
        timeFormatter = currentTimeFormatUiState.timeFormatter,
    )

    CompositionLocalProvider(LocalFormat provides format) {

        val navController = rememberNavController()

        val navBackStackEntry by navController.currentBackStackEntryAsState()

        val bottomBarState = rememberRouteState(topLevelRoutes)

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
            NavHost(navController, startDestination = RepeatsRoute) {
                composable<RepeatsRoute> { RepeatsScreen(
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

