package org.grakovne.lissen.repository

import android.content.Context
import android.net.Uri
import coil.ImageLoader
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer
import okio.buffer
import okio.source

class BookCoverFetcher(
    private val repository: ServerMediaRepository,
    private val uri: Uri,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val response = repository.fetchBookCover(uri.toString())

        return when (response) {
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
                    mimeType = "image/jpeg",
                    dataSource = coil.decode.DataSource.NETWORK
                )
            }
        }
    }
}

fun provideCustomImageLoader(context: Context, repository: ServerMediaRepository): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            add(BookCoverFetcherFactory(repository, context))
        }
        .build()
}

class BookCoverFetcherFactory(
    private val repository: ServerMediaRepository,
    private val context: Context
) : Fetcher.Factory<Uri> {
    override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher {
        return BookCoverFetcher(repository, data, context)
    }
}