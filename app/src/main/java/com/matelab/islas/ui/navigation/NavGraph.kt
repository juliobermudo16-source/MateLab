package com.matelab.islas.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matelab.islas.core.RootViewModel
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.ui.components.LocalHapticsEnabled
import com.matelab.islas.ui.components.LocalSoundManager
import com.matelab.islas.ui.screens.badges.BadgesScreen
import com.matelab.islas.ui.screens.collection.CollectionScreen
import com.matelab.islas.ui.screens.island.IslandScreen
import com.matelab.islas.ui.screens.map.MapScreen
import com.matelab.islas.ui.screens.mission.MissionScreen
import com.matelab.islas.ui.screens.onboarding.OnboardingScreen
import com.matelab.islas.ui.screens.profile.ProfileSetupScreen
import com.matelab.islas.ui.screens.review.ReviewScreen
import com.matelab.islas.ui.screens.settings.SettingsScreen
import com.matelab.islas.ui.screens.splash.SplashScreen
import com.matelab.islas.ui.screens.stats.StatsScreen
import com.matelab.islas.ui.theme.MateLabTheme

/**
 * Punto de entrada de la interfaz: tema, dependencias y grafo de navegacion.
 */
@Composable
fun MateLabApp(container: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        val root: RootViewModel = mateViewModel { RootViewModel(it) }
        val state by root.state.collectAsStateWithLifecycle()

        MateLabTheme(
            reducedMotion = !state.settings.animationsEnabled,
            bigText = state.settings.bigTextEnabled
        ) {
            CompositionLocalProvider(
                LocalSoundManager provides container.soundManager,
                LocalHapticsEnabled provides state.settings.hapticsEnabled
            ) {
                if (state.loading) {
                    SplashScreen()
                } else {
                    MateNavHost(startRoute = state.startRoute)
                }
            }
        }
    }
}

@Composable
private fun MateNavHost(startRoute: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startRoute,
        enterTransition = { slideInHorizontally(tween(280)) { it / 6 } + fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { slideOutHorizontally(tween(240)) { it / 6 } + fadeOut(tween(180)) }
    ) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onDone = {
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAP) {
            MapScreen(
                onOpenIsland = { navController.navigate(Routes.island(it)) },
                onOpenMission = { navController.navigate(Routes.mission(it)) },
                onOpenCollection = { navController.navigate(Routes.COLLECTION) },
                onOpenBadges = { navController.navigate(Routes.BADGES) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenReview = { navController.navigate(Routes.REVIEW) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.ISLAND,
            arguments = listOf(navArgument("worldId") { type = NavType.StringType })
        ) { entry ->
            val worldId = entry.arguments?.getString("worldId").orEmpty()
            IslandScreen(
                worldId = worldId,
                onBack = { navController.popBackStack() },
                onOpenMission = { navController.navigate(Routes.mission(it)) }
            )
        }

        composable(
            route = Routes.MISSION,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType })
        ) { entry ->
            val missionId = entry.arguments?.getString("missionId").orEmpty()
            MissionScreen(
                missionId = missionId,
                onExit = { navController.popBackStack() }
            )
        }

        composable(Routes.COLLECTION) {
            CollectionScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.BADGES) {
            BadgesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.REVIEW) {
            ReviewScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
