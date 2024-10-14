package org.grakovne.lissen.ui.screens.player.composable

import android.widget.Toast
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import org.grakovne.lissen.R
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun PlayerNavBarComposable(
    viewModel: PlayerViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    onChaptersClick: () -> Unit
) {
    val playingQueueExpanded by viewModel.playingQueueExpanded.observeAsState(false)
    val context = LocalContext.current

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = colorScheme.onBackground,
        modifier = modifier
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Headset,
                    contentDescription = stringResource(R.string.player_screen_library_navigation),
                )
            },
            label = { Text(stringResource(R.string.player_screen_library_navigation)) },
            selected = false,
            onClick = { navController.navigate("library_screen") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorScheme.primary,
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = stringResource(R.string.player_screen_timer_navigation)
                )
            },
            label = { Text(stringResource(R.string.player_screen_timer_navigation)) },
            selected = false,
            onClick = {
                Toast.makeText(
                    context,
                    "Timer Feature Under Construction Yet",
                    Toast.LENGTH_SHORT
                ).show()
            },
            enabled = true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorScheme.primary,
                unselectedIconColor = colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Book,
                    contentDescription = stringResource(R.string.player_screen_chapter_list_navigation)
                )
            },
            label = { Text(stringResource(R.string.player_screen_chapter_list_navigation)) },
            selected = playingQueueExpanded,
            onClick = { onChaptersClick() },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorScheme.primary,
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.player_screen_preferences_navigation)
                )
            },
            label = { Text(stringResource(R.string.player_screen_preferences_navigation)) },
            selected = false,
            onClick = { navController.navigate("settings_screen") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorScheme.primary,
            )
        )
    }
}