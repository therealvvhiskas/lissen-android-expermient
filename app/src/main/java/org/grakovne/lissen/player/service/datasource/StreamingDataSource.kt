package org.grakovne.lissen.player.service.datasource

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import kotlinx.coroutines.runBlocking
import org.grakovne.lissen.repository.ApiResult
import org.grakovne.lissen.repository.ServerMediaRepository
import java.io.InputStream


@UnstableApi
class StreamingDataSource(
    private val serverMediaRepository: ServerMediaRepository
) : DataSource {
    private var inputStream: InputStream? = null
    private var contentLength: Long = 0

    private lateinit var dataSpec: DataSpec


    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec

        val bookId = dataSpec.uri.getQueryParameter("bookId") ?: return 0
        val chapterId = dataSpec.uri.getQueryParameter("chapterId") ?: return 0

        val response = runBlocking { serverMediaRepository.fetchChapterContent(bookId, chapterId) }

        when (response) {
            is ApiResult.Error -> {
                println("Error loading data for mediaId: $bookId")
                contentLength = 0
            }

            is ApiResult.Success -> {
                inputStream = response.data
                contentLength = -1
            }
        }

        return contentLength
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return inputStream?.read(buffer, offset, length) ?: -1
    }

    override fun addTransferListener(transferListener: TransferListener) {
    }

    override fun close() {
        inputStream?.close()
        inputStream = null
    }

    override fun getUri(): Uri? {
        return if (::dataSpec.isInitialized) dataSpec.uri else null
    }
}