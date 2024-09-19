package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
@Composable
fun PlayerNavBarComposable(modifier: Modifier = Modifier) {
    val accentColor = colorScheme.primary

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = colorScheme.onBackground,
        modifier = modifier
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Headset,
                    contentDescription = "Fragments list",
                )
            },
            label = { Text("Library") },
            selected = false,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accentColor,
                selectedTextColor = accentColor,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = "Timer"
                )
            },
            label = { Text("Timer") },
            selected = false,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accentColor,
                selectedTextColor = accentColor,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Book,
                    contentDescription = "Chapters list"
                )
            },
            label = { Text("Chapters") },
            selected = false,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accentColor,
                selectedTextColor = accentColor,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Preferences"
                )
            },
            label = { Text("Settings") },
            selected = false,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accentColor,
                selectedTextColor = accentColor,
                indicatorColor = Color.Transparent
            )
        )
    }

}