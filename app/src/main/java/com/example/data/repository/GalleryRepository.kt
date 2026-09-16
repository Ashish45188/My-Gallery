package com.example.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.HiddenEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.TrashEntity
import com.example.data.model.AlbumItem
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GalleryRepository(
    private val context: Context,
    private val mediaStoreRepository: MediaStoreRepository,
    private val database: AppDatabase
) {
    private val favoritesDao = database.favoritesDao()
    private val hiddenDao = database.hiddenDao()
    private val trashDao = database.trashDao()
    private val settingsDao = database.settingsDao()

    suspend fun fetchAllMedia(): List<MediaItem> {
        return mediaStoreRepository.loadAllMedia()
    }

    suspend fun fetchImages(): List<MediaItem> {
        return mediaStoreRepository.loadImages()
    }

    suspend fun fetchVideos(): List<MediaItem> {
        return mediaStoreRepository.loadVideos()
    }

    fun getFavoriteIds(): Flow<List<Long>> = favoritesDao.getAllFavoriteIds()
    fun getFavorites(): Flow<List<FavoriteEntity>> = favoritesDao.getAllFavorites()

    fun isFavorite(mediaId: Long): Flow<Boolean> = favoritesDao.isFavorite(mediaId)

    suspend fun toggleFavorite(mediaItem: MediaItem, isCurrentlyFavorite: Boolean) = withContext(Dispatchers.IO) {
        if (isCurrentlyFavorite) {
            favoritesDao.deleteFavorite(mediaItem.id)
        } else {
            favoritesDao.insertFavorite(
                FavoriteEntity(
                    mediaId = mediaItem.id,
                    uriString = mediaItem.uri.toString(),
                    isVideo = mediaItem.isVideo,
                    dateAdded = System.currentTimeMillis()
                )
            )
        }
    }

    fun getHiddenIds(): Flow<List<Long>> = hiddenDao.getAllHiddenIds()
    fun getHiddenItems(): Flow<List<HiddenEntity>> = hiddenDao.getAllHidden()

    suspend fun hideItem(mediaItem: MediaItem) = withContext(Dispatchers.IO) {
        hiddenDao.insertHidden(
            HiddenEntity(
                mediaId = mediaItem.id,
                uriString = mediaItem.uri.toString(),
                isVideo = mediaItem.isVideo,
                dateHidden = System.currentTimeMillis(),
                originalPath = mediaItem.path,
                displayName = mediaItem.displayName,
                duration = mediaItem.duration,
                size = mediaItem.size,
                mimeType = mediaItem.mimeType,
                albumName = mediaItem.albumName
            )
        )
    }

    suspend fun unhideItem(mediaId: Long) = withContext(Dispatchers.IO) {
        hiddenDao.deleteHidden(mediaId)
    }

    suspend fun deleteHiddenPermanently(mediaId: Long) = withContext(Dispatchers.IO) {
        hiddenDao.deleteHidden(mediaId)
    }

    fun getTrashIds(): Flow<List<Long>> = trashDao.getAllTrashIds()
    fun getTrashItems(): Flow<List<TrashEntity>> = trashDao.getAllTrash()

    suspend fun moveToTrash(mediaItem: MediaItem) = withContext(Dispatchers.IO) {
        trashDao.insertTrash(
            TrashEntity(
                mediaId = mediaItem.id,
                uriString = mediaItem.uri.toString(),
                isVideo = mediaItem.isVideo,
                dateDeleted = System.currentTimeMillis(),
                originalPath = mediaItem.path,
                displayName = mediaItem.displayName,
                duration = mediaItem.duration,
                size = mediaItem.size,
                mimeType = mediaItem.mimeType,
                albumName = mediaItem.albumName
            )
        )
    }

    suspend fun restoreFromTrash(mediaId: Long) = withContext(Dispatchers.IO) {
        trashDao.deleteTrash(mediaId)
    }

    suspend fun permanentDeleteTrash(mediaId: Long) = withContext(Dispatchers.IO) {
        trashDao.deleteTrash(mediaId)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        trashDao.clearTrash()
    }

    fun getSettings(): Flow<SettingsEntity?> = settingsDao.getSettings()

    suspend fun saveSettings(settings: SettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.saveSettings(settings)
    }

    fun shareMedia(item: MediaItem) {
        mediaStoreRepository.shareMedia(item.uri, item.isVideo, "Share ${item.displayName}")
    }

    fun openVideo(item: MediaItem) {
        mediaStoreRepository.openVideoPlayer(item.uri)
    }

    fun groupIntoAlbums(mediaList: List<MediaItem>): List<AlbumItem> {
        val grouped = mediaList.groupBy { it.albumName }
        return grouped.map { (albumName, items) ->
            val latest = items.maxByOrNull { if (it.dateTaken > 0) it.dateTaken else it.dateModified * 1000 }
            AlbumItem(
                albumId = albumName,
                albumName = albumName,
                coverUri = latest?.uri,
                count = items.size,
                latestTimestamp = latest?.dateTaken ?: 0L
            )
        }.sortedByDescending { it.count }
    }
}
