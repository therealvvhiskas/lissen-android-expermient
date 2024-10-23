package org.grakovne.lissen.ui.extensions

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun Int.formatFully(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val remainingSeconds = this % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format("%02d:%02d", minutes, remainingSeconds)
    }
}

@SuppressLint("DefaultLocale")
fun Int.formatShortly(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60

    return String.format("%02dh %02dm", hours, minutes)
}

@SuppressLint("DefaultLocale")
fun Int.formatLeadingMinutes(): String {
    val minutes = this / 60
    val remainingSeconds = this % 60

    return String.format("%02d:%02d", minutes, remainingSeconds)
}
