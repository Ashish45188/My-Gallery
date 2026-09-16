package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val mediaId: Long,
    val uriString: String,
    val isVideo: Boolean,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "hidden_items")
data class HiddenEntity(
    @PrimaryKey val mediaId: Long,
    val uriString: String,
    val isVideo: Boolean,
    val dateHidden: Long = System.currentTimeMillis(),
    val originalPath: String = "",
    val displayName: String = "",
    val duration: Long = 0L,
    val size: Long = 0L,
    val mimeType: String = "",
    val albumName: String = ""
)

@Entity(tableName = "trash_items")
data class TrashEntity(
    @PrimaryKey val mediaId: Long,
    val uriString: String,
    val isVideo: Boolean,
    val dateDeleted: Long = System.currentTimeMillis(),
    val originalPath: String = "",
    val displayName: String = "",
    val duration: Long = 0L,
    val size: Long = 0L,
    val mimeType: String = "",
    val albumName: String = ""
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeOption: String = "SYSTEM",
    val gridColumns: Int = 3,
    val sortOption: String = "NEWEST",
    val pinHash: String = "",
    val isPinSet: Boolean = false
)
