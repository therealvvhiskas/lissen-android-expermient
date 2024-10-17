package org.grakovne.lissen.ui.screens.library.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.domain.Book

class LibraryPagingSource(
    private val dataProvider: AudiobookshelfChannel,
    private val libraryId: String,
) : PagingSource<Int, Book>() {
    override fun getRefreshKey(state: PagingState<Int, Book>) = state
        .anchorPosition
        ?.let { anchorPosition ->
            state
                .closestPageToPosition(anchorPosition)
                ?.prevKey
                ?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val currentPage = params.key ?: 0

        return dataProvider
            .fetchBooks(libraryId, currentPage, params.loadSize)
            .fold(
                onSuccess = {
                    LoadResult.Page(
                        data = it,
                        prevKey = if (currentPage == 0) null else currentPage - 1,
                        nextKey = if (it.isEmpty()) null else currentPage + 1
                    )
                },
                onFailure = {
                    LoadResult.Error(RuntimeException(""))
                }
            )

    }
}