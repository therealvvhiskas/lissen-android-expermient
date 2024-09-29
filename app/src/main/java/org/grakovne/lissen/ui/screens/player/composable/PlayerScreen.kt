package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.grakovne.lissen.viewmodel.PlayerViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    navController: NavController,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            PlayerNavBarComposable(
                viewModel,
                navController = navController,
                onChaptersClick = { viewModel.togglePlayingQueue() }
            )
        },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxHeight(),
        content = { innerPadding ->

            val playingQueueExpanded by viewModel.playingQueueExpanded.observeAsState(false)

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    visible = !playingQueueExpanded,
                    enter = expandVertically(animationSpec = tween(500)),
                    exit = shrinkVertically(animationSpec = tween(500))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TrackDetailsComposable(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        TrackControlComposable(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }

                PlayingQueueComposable(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (playingQueueExpanded) 5f else 2f)
                        .animateContentSize()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    )
}