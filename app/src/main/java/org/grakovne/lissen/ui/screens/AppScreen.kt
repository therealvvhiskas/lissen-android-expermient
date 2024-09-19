package org.grakovne.lissen.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import org.grakovne.lissen.ui.navigation.AppNavHost

@Composable
fun AppScreen() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}