package com.eden.alarmplus.main.ui.alarms

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.alarmplus.ui.theme.AlarmPlusTheme
import kotlinx.serialization.Serializable

@Serializable
object AlarmsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen() {

}


@PreviewScreenSizes
@Composable
fun AlarmsScreenPreview() {
    val navController = rememberNavController()

    AlarmPlusTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {}
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = AlarmsRoute, Modifier.padding(innerPadding)) {
                composable<AlarmsRoute> { AlarmsScreen() }
            }
        }
    }
}