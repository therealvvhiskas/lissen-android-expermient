package org.grakovne.lissen.channel.audiobookshelf.model

data class StartPlaybackRequest(
    val deviceInfo: org.grakovne.lissen.channel.audiobookshelf.model.DeviceInfo,
    val supportedMimeTypes: List<String>,
    val mediaPlayer: String,
    val forceTranscode: Boolean,
    val forceDirectPlay: Boolean
)

data class DeviceInfo(
    val clientName: String,
    val deviceId: String,
    val deviceName: String
)
