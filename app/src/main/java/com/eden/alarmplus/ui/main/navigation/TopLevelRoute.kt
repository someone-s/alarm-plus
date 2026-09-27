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
        R.string.ui_main_maincontent_alarms,
        AlarmsRoute,
        R.drawable.alarm_filled,
        R.drawable.alarm
    ),
    TopLevelRoute(
        R.string.ui_main_maincontent_about,
        AboutRoute,
        R.drawable.info_filled,
        R.drawable.info
    ),
)

@Serializable
object AlarmsRoute

@Serializable
object AboutRoute