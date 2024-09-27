package org.grakovne.lissen.ui.extensions


fun Int.hhmmss(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val remainingSeconds = this % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format("%02d:%02d", minutes, remainingSeconds)
    }
}

fun Int.hhmm(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60

    return String.format("%02dh %02dm", hours, minutes)
}
