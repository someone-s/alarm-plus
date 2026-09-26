package com.eden.alarmplus.ui.main.about

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
object AboutRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {

}

@PreviewScreenSizes
@Composable
fun AboutScreenPreview() {
    val navController = rememberNavController()

    AlarmPlusTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {}
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = AboutRoute, Modifier.padding(innerPadding)) {
                composable<AboutRoute> { AboutScreen() }
            }
        }
    }
}