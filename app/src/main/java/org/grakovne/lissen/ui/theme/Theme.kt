package org.grakovne.lissen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val LightColorScheme = lightColorScheme(
    primary = FoxOrange,
    secondary = Dark,
    tertiary = FoxOrange
)


@Composable
fun LissenTheme(
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val backgroundColor = MaterialTheme.colorScheme.background

    SideEffect {
        systemUiController.setNavigationBarColor(
            color = backgroundColor,
            darkIcons = false
        )

        systemUiController.setStatusBarColor(
            color = backgroundColor,
            darkIcons = true
        )
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}