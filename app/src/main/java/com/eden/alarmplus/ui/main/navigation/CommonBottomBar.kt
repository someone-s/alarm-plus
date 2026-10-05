package com.eden.alarmplus.ui.main.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun rememberCommonBottomBarState(
    initialTopLevelRoutes: List<TopLevelRoute<*>>,
): CommonBottomBarState {
    return remember {
        CommonBottomBarState(
            initialTopLevelRoutes
        )
    }
}

@Stable
class CommonBottomBarState(
    initialTopLevelRoutes: List<TopLevelRoute<*>>,
) {
    var currentDestination by mutableStateOf<NavDestination?>(null)
    val topLevelRoutes = initialTopLevelRoutes.toMutableStateList()
    var targetRoute by mutableStateOf<TopLevelRoute<*>?>(null)
}

@Composable
fun CommonBottomBar(
    state: CommonBottomBarState,
)  {
    val hapticFeedback = LocalHapticFeedback.current

    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        state.topLevelRoutes.forEach { topLevelRoute ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            if (state.currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true)
                                topLevelRoute.selectedIcon
                            else
                                topLevelRoute.unselectedIcon
                        ),
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(topLevelRoute.name)) },
                selected = state.currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    state.targetRoute = topLevelRoute
                },
            )
        }
    }
}