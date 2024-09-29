package org.grakovne.lissen.repository

import android.content.Context
import android.net.Uri
import coil.ImageLoader
import coil.decode.ImageSource
import coil.disk.DiskCache
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.memory.MemoryCache
import coil.request.Options
import okio.Buffer
import okio.buffer
import okio.source

class BookCoverFetcher(
    private val repository: ServerMediaRepository,
    private val uri: Uri,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult? =
        when (val response = repository.fetchBookCover(uri.toString())) {
            is ApiResult.Error -> {
                null
            }

            is ApiResult.Success -> {
                val bytes = response.data

                val buffer = Buffer().write(bytes)
                val source = buffer.inputStream().source().buffer()

                val imageSource = ImageSource(source, context)

                SourceResult(
                    source = imageSource,
                    mimeType = null,
                    dataSource = coil.decode.DataSource.NETWORK
                )
            }
        }
}

fun provideCustomImageLoader(context: Context, repository: ServerMediaRepository) =
    ImageLoader
        .Builder(context)
        .components { add(BookCoverFetcherFactory(repository, context)) }
        .memoryCache {
            MemoryCache.Builder(context)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("сover_cache"))
                .build()
        }
        .build()

class BookCoverFetcherFactory(
    private val repository: ServerMediaRepository,
    private val context: Context
) : Fetcher.Factory<Uri> {

    override fun create(data: Uri, options: Options, imageLoader: ImageLoader) = BookCoverFetcher(repository, data, context)
}