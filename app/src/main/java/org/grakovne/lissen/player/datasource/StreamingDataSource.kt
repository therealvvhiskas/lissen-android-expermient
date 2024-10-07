package org.grakovne.lissen.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.grakovne.lissen.repository.ApiResult
import org.grakovne.lissen.repository.ServerMediaRepository
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

@UnstableApi
class StreamingDataSource(
    private val repository: ServerMediaRepository,
    private val chapterId: String,
    private val coroutineScope: CoroutineScope
) : DataSource {

    private val pipedOutputStream = PipedOutputStream()
    private val pipedInputStream = PipedInputStream(pipedOutputStream)

    private val byteChannel = Channel<ByteArray>(Channel.UNLIMITED)

    override fun open(dataSpec: DataSpec): Long {
        coroutineScope.launch {
            val result = repository.fetchChapterContent(chapterId)
            if (result is ApiResult.Success) {
                pipedOutputStream.use { output ->
                    output.write(result.data)
                }
                byteChannel.close()
            } else {
                byteChannel.close(IOException("Failed to fetch data"))
            }
        }
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return pipedInputStream.read(buffer, offset, readLength)
    }

    override fun addTransferListener(transferListener: TransferListener) {

    }

    override fun close() {
        pipedInputStream.close()
        pipedOutputStream.close()
    }

    override fun getUri(): Uri? = null
    override fun getResponseHeaders(): Map<String, List<String>> = emptyMap()
}