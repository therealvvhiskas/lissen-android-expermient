package org.grakovne.lissen.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onRateApp: () -> Unit,
    onOpenGitHub: () -> Unit
) {
    Scaffold(
        topBar = { Spacer(modifier = Modifier.height(24.dp)) },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxHeight(),
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                ServerSection()
                LibrarySection()
                AdditionalSection({}, {})
            }
        }
    )
}

@Composable
fun LibrarySection() {
    val libraries = listOf(
        "Historical",
        "Sci-Fi",
        "Tales"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedLibrary by remember { mutableStateOf(libraries.first()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Active Library",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box { // Изменено с Row на Box для корректного отображения DropdownMenu
            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(selectedLibrary)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ArrowDropUp else Icons.Outlined.ArrowDropDown,
                    contentDescription = null
                )
            }

            MaterialTheme() {
                DropdownMenu(
                    expanded = expanded,
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    libraries.forEach { library ->
                        DropdownMenuItem(
                            text = { Text(library) },
                            onClick = {
                                selectedLibrary = library
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ServerSection() {
    var serverUrl by remember { mutableStateOf("https://example.com") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Настройки сервера",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("URL сервера") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Логин") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),

                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AdditionalSection(onRateApp: () -> Unit, onOpenGitHub: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Дополнительно",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.StarRate,
                        contentDescription = "Оценить приложение",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                headlineContent = { Text("Оценить приложение") },
                modifier = Modifier
                    .clickable { onRateApp() }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "GitHub автора",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                headlineContent = { Text("GitHub автора") },
                modifier = Modifier
                    .clickable { onOpenGitHub() }
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        }
    }
}

