package org.grakovne.lissen.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.grakovne.lissen.ui.navigation.AppNavHost
import org.grakovne.lissen.ui.theme.LissenTheme

@AndroidEntryPoint
class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LissenTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}