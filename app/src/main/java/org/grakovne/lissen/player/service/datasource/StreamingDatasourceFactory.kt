package org.grakovne.lissen.player.service.datasource

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import org.grakovne.lissen.repository.ServerMediaRepository


@UnstableApi
class StreamingDatasourceFactory(
    private val serverMediaRepository: ServerMediaRepository
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return StreamingDataSource(serverMediaRepository)
    }
}