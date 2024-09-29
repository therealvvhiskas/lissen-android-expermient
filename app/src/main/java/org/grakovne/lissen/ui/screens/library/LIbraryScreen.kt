package org.grakovne.lissen.ui.screens.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dagger.hilt.android.EntryPointAccessors
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.ui.components.ImageLoaderEntryPoint
import org.grakovne.lissen.ui.screens.library.composables.LibraryComposable
import org.grakovne.lissen.ui.screens.library.composables.MiniPlayerComposable
import org.grakovne.lissen.ui.screens.library.composables.RecentBooksComposable
import org.grakovne.lissen.viewmodel.LibraryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val titleTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val titleHeightDp = with(LocalDensity.current) { titleTextStyle.lineHeight.toPx().toDp() }

    val listState = rememberLazyListState()

    val books by viewModel.books.observeAsState(emptyList())
    val recentBooks: List<RecentBook> by viewModel.recentBooks.observeAsState(emptyList())

    val context = LocalContext.current
    val imageLoader = remember {
        EntryPointAccessors.fromApplication(context, ImageLoaderEntryPoint::class.java)
            .getImageLoader()
    }

    val navBarTitle by remember {
        derivedStateOf {
            val firstVisibleItemIndex = listState.firstVisibleItemIndex
            if (firstVisibleItemIndex >= 1) {
                "Library"
            } else {
                "Continue Listening"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Crossfade(targetState = navBarTitle, label = "navbar_title_fade") { title ->
                        Text(
                            text = title,
                            style = titleTextStyle
                        )
                    }
                },
                modifier = Modifier.systemBarsPadding()
            )
        },
        bottomBar = {
            MiniPlayerComposable(navController)
        },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        content = { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "recent_books") {
                    RecentBooksComposable(recentBooks = recentBooks, imageLoader)
                }

                item(key = "library_title") {
                    AnimatedContent(
                        targetState = navBarTitle,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                                animationSpec = tween(
                                    300
                                )
                            )
                        }, label = "library_header_fade"
                    ) {
                        if (it != "Library") {
                            Text(
                                style = titleTextStyle,
                                text = "Library",
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(titleHeightDp)
                            )
                        }
                    }
                }

                item(key = "library_list") {
                    LibraryComposable(books = books, imageLoader)
                }
            }
        }
    )
}