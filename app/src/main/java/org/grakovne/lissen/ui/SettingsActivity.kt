package org.grakovne.lissen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.grakovne.lissen.ui.screens.settings.SettingsScreen
import org.grakovne.lissen.ui.theme.LissenTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LissenTheme {
                SettingsScreen({}, {}, {})
            }
        }
    }
}
