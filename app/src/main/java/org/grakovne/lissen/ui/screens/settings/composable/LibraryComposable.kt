package org.grakovne.lissen.ui.screens.settings.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.ui.screens.settings.Library
import org.grakovne.lissen.viewmodel.ConnectionViewModel

@Composable
fun LibraryComposable(viewModel: ConnectionViewModel) {
    val isServerConnected by viewModel.isConnected.observeAsState(false)
    val libraries by viewModel.libraries.observeAsState(
        listOf(
            Library("id", "Sci-Hub"),
            Library("id", "Fiction"),
            Library("id", "Tales"),
        )
    )
    val preferredLibrary by viewModel.preferredLibrary.observeAsState(Library("id", "Sci-Hub"))

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Preferred Library",
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box {
            OutlinedButton(
                onClick = { if (isServerConnected) expanded = !expanded },
                modifier = Modifier.fillMaxWidth(),
                enabled = isServerConnected
            ) {
                Text(preferredLibrary.title)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ArrowDropUp else Icons.Outlined.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                modifier = Modifier
                    .background(color = colorScheme.background),
                onDismissRequest = { expanded = false }
            ) {
                libraries
                    .forEach { library ->
                        DropdownMenuItem(
                            text = { Text(library.title) },
                            onClick = {
                                if (isServerConnected) {
                                    viewModel.preferLibrary(library)
                                }
                                expanded = false
                            },
                            enabled = true
                        )
                    }
            }
        }
    }
}