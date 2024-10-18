package org.grakovne.lissen.ui.screens.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.AirplanemodeInactive
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.grakovne.lissen.R
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.ui.components.ImageLoaderEntryPoint
import org.grakovne.lissen.ui.extensions.withMinimumTime
import org.grakovne.lissen.ui.screens.library.composables.BookComposable
import org.grakovne.lissen.ui.screens.library.composables.MiniPlayerComposable
import org.grakovne.lissen.ui.screens.library.composables.RecentBooksComposable
import org.grakovne.lissen.ui.screens.library.composables.placeholder.LibraryPlaceholderComposable
import org.grakovne.lissen.ui.screens.library.composables.placeholder.RecentBooksPlaceholderComposable
import org.grakovne.lissen.viewmodel.CachingModelView
import org.grakovne.lissen.viewmodel.LibraryViewModel
import org.grakovne.lissen.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    cachingModelView: CachingModelView = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {

    val coroutineScope = rememberCoroutineScope()

    val library: LazyPagingItems<Book> = libraryViewModel.libraryPager.collectAsLazyPagingItems()
    val recentBooks: List<RecentBook> by libraryViewModel.recentBooks.observeAsState(emptyList())

    val recentBookRefreshing by libraryViewModel.recentBookUpdating.observeAsState(false)
    var pullRefreshing by remember { mutableStateOf(false) }

    fun refreshContent(showRefreshing: Boolean) {
        coroutineScope.launch {
            if (showRefreshing) {
                pullRefreshing = true
            }

            withMinimumTime(500) {
                listOf(
                    async { library.refresh() },
                    async { libraryViewModel.fetchRecentListening() },
                ).awaitAll()
            }

            pullRefreshing = false
        }
    }

    val isContentLoading by remember {
        derivedStateOf {
            pullRefreshing
                    || recentBookRefreshing
                    || library.loadState.refresh is LoadState.Loading
        }
    }

    var navigationItemSelected by remember { mutableStateOf(false) }


    val pullRefreshState = rememberPullRefreshState(
        refreshing = pullRefreshing,
        onRefresh = {
            refreshContent(showRefreshing = true)
        }
    )

    val titleTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val titleHeightDp = with(LocalDensity.current) { titleTextStyle.lineHeight.toPx().toDp() }

    val listState = rememberLazyListState()

    val playingBook by playerViewModel.book.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { libraryViewModel.refreshRecentListening() }

    val imageLoader = remember {
        EntryPointAccessors
            .fromApplication(context, ImageLoaderEntryPoint::class.java)
            .getImageLoader()
    }

    val navBarTitle by remember {
        derivedStateOf {
            val firstVisibleItemIndex = listState.firstVisibleItemIndex
            when {
                firstVisibleItemIndex >= 1 -> context.getString(R.string.library_screen_library_title)
                else -> context.getString(R.string.library_screen_continue_listening_title)

            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    IconButton(onClick = { navigationItemSelected = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Menu"
                        )
                    }
                    DropdownMenu(
                        expanded = navigationItemSelected,
                        onDismissRequest = { navigationItemSelected = false },
                        modifier = Modifier
                            .background(colorScheme.background)
                            .padding(8.dp)
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = if (libraryViewModel.isCacheForce()) Icons.Outlined.AirplanemodeInactive else Icons.Outlined.AirplanemodeActive,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(
                                    text = if (libraryViewModel.isCacheForce()) "Disable Offline" else "Enable Offline",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            onClick = {
                                navigationItemSelected = false
                                libraryViewModel.toggleCacheForce()
                                refreshContent(showRefreshing = false)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(
                                    stringResource(R.string.library_screen_preferences_menu_item),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            onClick = {
                                navigationItemSelected = false
                                navController.navigate("settings_screen")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.alpha(0.6f),
                                    imageVector = Icons.Outlined.Flag,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(
                                    stringResource(R.string.library_screen_report_issue_menu_item),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .alpha(0.6f)
                                )
                            },
                            onClick = { navigationItemSelected = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                },
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
            playingBook?.let {
                MiniPlayerComposable(
                    navController = navController,
                    book = it,
                    imageLoader = imageLoader,
                    playerViewModel = playerViewModel
                )
            }
        },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .pullRefresh(pullRefreshState)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {

                    item(key = "recent_books") {
                        if (isContentLoading) {
                            RecentBooksPlaceholderComposable()
                        } else {
                            RecentBooksComposable(
                                navController = navController,
                                recentBooks = recentBooks,
                                imageLoader = imageLoader
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    item(key = "library_title") {
                        AnimatedContent(
                            targetState = navBarTitle,
                            transitionSpec = {
                                fadeIn(
                                    animationSpec =
                                    tween(300)
                                ) togetherWith fadeOut(
                                    animationSpec = tween(
                                        300
                                    )
                                )
                            }, label = "library_header_fade"
                        ) {
                            if (it != stringResource(R.string.library_screen_library_title)) {
                                Text(
                                    style = titleTextStyle,
                                    text = stringResource(R.string.library_screen_library_title),
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

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    if (isContentLoading) {
                        item {
                            LibraryPlaceholderComposable()
                        }
                    } else {
                        items(count = library.itemCount) {
                            BookComposable(
                                book = library[it] ?: return@items,
                                imageLoader = imageLoader,
                                navController = navController,
                                cachingModelView = cachingModelView
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = pullRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    )
}