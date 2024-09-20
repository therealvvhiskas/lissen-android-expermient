package org.grakovne.lissen.ui.navigation

import LoginScreen
import PlayerScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.grakovne.lissen.ui.screens.settings.SettingsScreen
import org.grakovne.lissen.viewmodel.ConnectionViewModel
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "player_screen"
    ) {

        composable("player_screen") {
            PlayerScreen(viewModel = PlayerViewModel(), navController)
        }

        composable("login_screen") {
            LoginScreen( navController)
        }

        composable("settings_screen") {
            SettingsScreen(
                viewModel = ConnectionViewModel(),
                onBack = { navController.popBackStack() })
        }
    }
}
