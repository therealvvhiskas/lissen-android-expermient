package org.grakovne.lissen.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.grakovne.lissen.ui.screens.AppScreen
import org.grakovne.lissen.ui.theme.LissenTheme
import org.grakovne.lissen.viewmodel.ConnectionViewModel

@AndroidEntryPoint
class AppActivity : ComponentActivity() {
    val viewModel: ConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            enableEdgeToEdge()
            setContent {
                LissenTheme {
                    AppScreen()
                }
            }
        }
    }
}