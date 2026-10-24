package com.eden.alarmplus.ui.main.about

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eden.alarmplus.ui.main.about.features.FeaturesSection
import com.eden.alarmplus.ui.main.navigation.AboutRoute
import com.eden.alarmplus.ui.main.navigation.CommonRouteState
import com.eden.alarmplus.ui.main.navigation.populateScope
import com.eden.alarmplus.ui.theme.AlarmPlusTheme

@Composable
fun AboutScreen(
    sharedContentKey: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomBarState: CommonRouteState,
) = with(sharedTransitionScope)  {

    val hapticFeedback = LocalHapticFeedback.current

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
            topBar = { AboutTopBar(scrollBehavior) },
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
                        item { FeaturesSection() }
                    }
                }

            }
        )
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
                    bottomBarState = CommonRouteState(listOf()),
                ) }
            }
        }
    }
}