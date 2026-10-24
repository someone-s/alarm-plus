package com.eden.alarmplus.ui.main.navigation

import com.eden.alarmplus.R
import kotlinx.serialization.Serializable


data class TopLevelRoute<T : Any>(
    val name: Int,
    val route: T,
    val selectedIcon: Int,
    val unselectedIcon: Int
)

val topLevelRoutes = listOf(
    TopLevelRoute(
        R.string.ui_main_navigation_toplevelroute_repeats,
        RepeatsRoute,
        R.drawable.event_repeat_filled,
        R.drawable.event_repeat
    ),
    TopLevelRoute(
        R.string.ui_main_navigation_toplevelroute_about,
        AboutRoute,
        R.drawable.info_filled,
        R.drawable.info
    ),
)

@Serializable
object RepeatsRoute

@Serializable
object AboutRoute