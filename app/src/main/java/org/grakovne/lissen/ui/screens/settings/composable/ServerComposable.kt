package org.grakovne.lissen.ui.screens.settings.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.viewmodel.ConnectionViewModel

@Composable
fun ServerComposable(viewModel: ConnectionViewModel) {
    val serverUrl by viewModel.url.observeAsState("")
    val login by viewModel.login.observeAsState("")

    var password by remember { mutableStateOf("") }
    var passwordVisibility: Boolean by remember { mutableStateOf(false) }

    Column() {
        Text(
            text = "Server Connection",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        ListItem(
            headlineContent = {
                Row(
                    modifier = Modifier

                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Podcasts,
                        contentDescription = "Preferences",
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .size(24.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                modifier = Modifier.padding(start = 10.dp),
                                text = "audiobook.grakovne.org",
                                style = TextStyle(fontFamily = FontFamily.Monospace)
                            )
                        }
                        Text(
                            modifier = Modifier.padding(start = 10.dp, top = 4.dp),
                            text = "2.13.4",
                            style = TextStyle(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            },
            trailingContent = {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Close",
                    modifier = Modifier.size(24.dp)
                )
            }
        )

//
//        Button(
//            onClick = {  },
//            colors = ButtonDefaults.buttonColors(Color.Gray)
//        ) {
//            Text(text = "Disconnect")
//        }


//        OutlinedTextField(
//            value = serverUrl,
//            onValueChange = { viewModel.updateServerUrl(it) },
//            label = { Text("Server URL") },
//            shape = RoundedCornerShape(16.dp),
//            singleLine = true,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 4.dp)
//        )
//
//        OutlinedTextField(
//            value = login,
//            onValueChange = { viewModel.updateLogin(it) },
//            label = { Text("Login") },
//            shape = RoundedCornerShape(16.dp),
//            singleLine = true,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 4.dp)
//        )
//
//        OutlinedTextField(
//            value = password,
//            visualTransformation = if (!passwordVisibility) PasswordVisualTransformation() else VisualTransformation.None,
//            onValueChange = { password = it },
//            trailingIcon = {
//                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
//                    Icon(
//                        imageVector = if (passwordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
//                        contentDescription = "Show Password"
//                    )
//                }
//            },
//            label = { Text("Password") },
//            shape = RoundedCornerShape(16.dp),
//            singleLine = true,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 4.dp)
//        )

//        Spacer(modifier = Modifier.height(16.dp))

//
//        Button(
//            onClick = {},
//            modifier = Modifier
//                .fillMaxWidth(0.5f)
//                .padding(top = 4.dp)
//                .align(Alignment.CenterHorizontally),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Text(text = "Disconnect")
//        }
    }
}