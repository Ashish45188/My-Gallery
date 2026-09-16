package com.example.data.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val albumName: String,
    val dateTaken: Long,
    val dateModified: Long,
    val size: Long,
    val mimeType: String,
    val isVideo: Boolean,
    val duration: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val path: String = ""
) {
    val formattedDuration: String
        get() {
            if (!isVideo || duration <= 0) return ""
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            var tempSize = size.toDouble()
            var unitIndex = 0
            while (tempSize >= 1024 && unitIndex < units.size - 1) {
                tempSize /= 1024
                unitIndex++
            }
            return String.format("%.1f %s", tempSize, units[unitIndex])
        }
}

data class AlbumItem(
    val albumId: String,
    val albumName: String,
    val coverUri: Uri?,
    val count: Int,
    val latestTimestamp: Long
)

enum class SortOption(val title: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    NAME("Name (A to Z)"),
    SIZE("File size (Largest)")
}

enum class ThemeOption(val title: String) {
    SYSTEM("System default"),
    LIGHT("Light mode"),
    DARK("Dark mode")
}
