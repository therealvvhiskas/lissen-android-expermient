package org.grakovne.lissen.ui.screens.settings.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.R
import org.grakovne.lissen.ui.screens.library.PreferredLibrarySettingComposable
import org.grakovne.lissen.viewmodel.PlayerViewModel
import org.grakovne.lissen.viewmodel.SettingsViewModel

@Composable
fun PreferredLibrarySettingsComposable(
    viewModel: SettingsViewModel,
    playerViewModel: PlayerViewModel,
) {
    val libraries by viewModel.libraries.observeAsState(emptyList())
    val preferredLibrary by viewModel.preferredLibrary.observeAsState()
    var preferredLibraryExpanded by remember { mutableStateOf(false) }

    SideEffect {
        viewModel.fetchLibraries()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { preferredLibraryExpanded = true }
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_screen_preferred_library_title),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = preferredLibrary?.title
                    ?: stringResource(R.string.library_is_not_available),
                style = typography.bodyMedium,
                color = when (preferredLibrary?.title) {
                    null -> colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else -> colorScheme.onSurfaceVariant
                },
            )
        }
    }

    if (preferredLibraryExpanded && libraries != null && libraries.isNotEmpty()) {
        PreferredLibrarySettingComposable(
            libraries = libraries,
            preferredLibrary = preferredLibrary,
            onDismissRequest = { preferredLibraryExpanded = false },
            onItemSelected = {
                viewModel.preferLibrary(it)
                playerViewModel.clearPlayingBook()
            },
        )
    }
}
