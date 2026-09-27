package com.eden.alarmplus.ui.main.about

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.alarmplus.ui.main.navigation.AboutRoute
import com.eden.alarmplus.ui.main.navigation.CommonBottomBar
import com.eden.alarmplus.ui.main.navigation.CommonBottomBarState
import com.eden.alarmplus.ui.theme.AlarmPlusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    sharedContentKey: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomBarState: CommonBottomBarState,
) = with(sharedTransitionScope)  {
    Scaffold(
        modifier = Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(sharedContentKey),
            animatedVisibilityScope = animatedVisibilityScope
        ),
        bottomBar = {
            CommonBottomBar(state = bottomBarState)
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding))
    }
}

@PreviewScreenSizes
@Composable
fun AboutScreenPreview() {
    val navController = rememberNavController()

    AlarmPlusTheme {
        SharedTransitionLayout {
            NavHost(navController, startDestination = AboutRoute) {
                composable<AboutRoute> { AboutScreen(
                    sharedContentKey = "SCAFFOLD",
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    bottomBarState = CommonBottomBarState(listOf()),
                ) }
            }
        }
    }
}