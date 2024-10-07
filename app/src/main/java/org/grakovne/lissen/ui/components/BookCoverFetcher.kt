package org.grakovne.lissen.ui.components

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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okio.buffer
import okio.source
import org.grakovne.lissen.repository.ApiResult
import org.grakovne.lissen.repository.ServerMediaRepository
import javax.inject.Singleton

class BookCoverFetcher(
    private val repository: ServerMediaRepository,
    private val uri: Uri,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult? =
        when (val response = repository.fetchBookCover(uri.toString())) {
            is ApiResult.Error -> null
            is ApiResult.Success -> {
                val stream = response.data
                val source = stream.source().buffer()
                val imageSource = ImageSource(source, context)

                SourceResult(
                    source = imageSource,
                    mimeType = null,
                    dataSource = coil.decode.DataSource.NETWORK
                )
            }
        }
}

class BookCoverFetcherFactory(
    private val repository: ServerMediaRepository,
    private val context: Context
) : Fetcher.Factory<Uri> {

    override fun create(data: Uri, options: Options, imageLoader: ImageLoader) =
        BookCoverFetcher(repository, data, context)
}

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Singleton
    @Provides
    fun provideCustomImageLoader(
        @ApplicationContext context: Context,
        repository: ServerMediaRepository
    ): ImageLoader {
        return ImageLoader
            .Builder(context)
            .components {
                add(BookCoverFetcherFactory(repository, context))
            }
            .memoryCache {
                MemoryCache.Builder(context).build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("сover_cache"))
                    .build()
            }
            .build()
    }
}
