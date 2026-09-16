package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SettingsEntity
import com.example.data.model.AlbumItem
import com.example.data.model.MediaItem
import com.example.data.model.SortOption
import com.example.data.model.ThemeOption
import com.example.data.repository.GalleryRepository
import com.example.data.repository.MediaStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import java.security.MessageDigest

enum class GalleryTab(val title: String) {
    PHOTOS("Photos"),
    ALBUMS("Albums"),
    VIDEOS("Videos"),
    FAVORITES("Favorites")
}

data class GalleryUiState(
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val selectedTab: GalleryTab = GalleryTab.PHOTOS,
    val selectedAlbumName: String? = null,
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val sortOption: SortOption = SortOption.NEWEST,
    val gridColumns: Int = 3,
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val isHiddenVaultUnlocked: Boolean = false,
    val isPinSet: Boolean = false,
    val activeViewerItem: MediaItem? = null,
    val currentViewerIndex: Int = 0,
    val isRecentlyDeletedOpen: Boolean = false,
    val isHiddenVaultOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val userMessage: String? = null
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaStoreRepo = MediaStoreRepository(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = GalleryRepository(application, mediaStoreRepo, database)

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val _rawMedia = MutableStateFlow<List<MediaItem>>(emptyList())

    val favoriteIds = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenIds = repository.getHiddenIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashIds = repository.getTrashIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenItems = repository.getHiddenItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashItems = repository.getTrashItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            settings.collect { settingEntity ->
                if (settingEntity != null) {
                    _uiState.value = _uiState.value.copy(
                        gridColumns = settingEntity.gridColumns,
                        themeOption = runCatching { ThemeOption.valueOf(settingEntity.themeOption) }.getOrDefault(ThemeOption.SYSTEM),
                        sortOption = runCatching { SortOption.valueOf(settingEntity.sortOption) }.getOrDefault(SortOption.NEWEST),
                        isPinSet = settingEntity.isPinSet
                    )
                }
            }
        }
    }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(hasPermission = true)
        loadMedia()
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val media = repository.fetchAllMedia()
            _rawMedia.value = media
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectTab(tab: GalleryTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            selectedAlbumName = null,
            isRecentlyDeletedOpen = false,
            isHiddenVaultOpen = false,
            isSettingsOpen = false
        )
    }

    fun openAlbum(albumName: String) {
        _uiState.value = _uiState.value.copy(selectedAlbumName = albumName)
    }

    fun closeAlbum() {
        _uiState.value = _uiState.value.copy(selectedAlbumName = null)
    }

    fun openRecentlyDeleted() {
        _uiState.value = _uiState.value.copy(
            isRecentlyDeletedOpen = true,
            isHiddenVaultOpen = false,
            isSettingsOpen = false
        )
    }

    fun closeRecentlyDeleted() {
        _uiState.value = _uiState.value.copy(isRecentlyDeletedOpen = false)
    }

    fun openHiddenVault() {
        _uiState.value = _uiState.value.copy(
            isHiddenVaultOpen = true,
            isRecentlyDeletedOpen = false,
            isSettingsOpen = false
        )
    }

    fun closeHiddenVault() {
        _uiState.value = _uiState.value.copy(
            isHiddenVaultOpen = false,
            isHiddenVaultUnlocked = false
        )
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(
            isSettingsOpen = true,
            isRecentlyDeletedOpen = false,
            isHiddenVaultOpen = false
        )
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(isSettingsOpen = false)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleSearch(open: Boolean? = null) {
        val newState = open ?: !_uiState.value.isSearchOpen
        _uiState.value = _uiState.value.copy(
            isSearchOpen = newState,
            searchQuery = if (!newState) "" else _uiState.value.searchQuery
        )
    }

    fun setSortOption(sort: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sort)
        saveSettingsToDb()
    }

    fun setGridColumns(cols: Int) {
        _uiState.value = _uiState.value.copy(gridColumns = cols.coerceIn(2, 4))
        saveSettingsToDb()
    }

    fun setThemeOption(theme: ThemeOption) {
        _uiState.value = _uiState.value.copy(themeOption = theme)
        saveSettingsToDb()
    }

    private fun saveSettingsToDb() {
        viewModelScope.launch {
            val currentSettings = settings.value
            repository.saveSettings(
                SettingsEntity(
                    id = 1,
                    themeOption = _uiState.value.themeOption.name,
                    gridColumns = _uiState.value.gridColumns,
                    sortOption = _uiState.value.sortOption.name,
                    pinHash = currentSettings?.pinHash ?: "",
                    isPinSet = currentSettings?.isPinSet ?: false
                )
            )
        }
    }

    fun unlockVault(pin: String): Boolean {
        val currentSettings = settings.value
        val storedHash = currentSettings?.pinHash
        val enteredHash = hashPin(pin)

        return if (!storedHash.isNullOrEmpty() && storedHash == enteredHash) {
            _uiState.value = _uiState.value.copy(isHiddenVaultUnlocked = true)
            true
        } else {
            false
        }
    }

    fun setVaultPin(pin: String) {
        viewModelScope.launch {
            val hashed = hashPin(pin)
            val currentSettings = settings.value
            repository.saveSettings(
                SettingsEntity(
                    id = 1,
                    themeOption = _uiState.value.themeOption.name,
                    gridColumns = _uiState.value.gridColumns,
                    sortOption = _uiState.value.sortOption.name,
                    pinHash = hashed,
                    isPinSet = true
                )
            )
            _uiState.value = _uiState.value.copy(isPinSet = true, isHiddenVaultUnlocked = true)
        }
    }

    fun lockVault() {
        _uiState.value = _uiState.value.copy(isHiddenVaultUnlocked = false)
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun openViewer(item: MediaItem, fullList: List<MediaItem>) {
        val index = fullList.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            activeViewerItem = item,
            currentViewerIndex = index
        )
    }

    fun closeViewer() {
        _uiState.value = _uiState.value.copy(activeViewerItem = null)
    }

    fun setViewerIndex(index: Int, fullList: List<MediaItem>) {
        if (index in fullList.indices) {
            _uiState.value = _uiState.value.copy(
                currentViewerIndex = index,
                activeViewerItem = fullList[index]
            )
        }
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(item.id)
            repository.toggleFavorite(item, isFav)
        }
    }

    fun hideItem(item: MediaItem) {
        viewModelScope.launch {
            repository.hideItem(item)
            _uiState.value = _uiState.value.copy(
                activeViewerItem = null,
                userMessage = "Moved to Hidden Vault"
            )
        }
    }

    fun unhideItem(mediaId: Long) {
        viewModelScope.launch {
            repository.unhideItem(mediaId)
            _uiState.value = _uiState.value.copy(userMessage = "Restored to All Photos")
        }
    }

    fun deleteHiddenPermanently(mediaId: Long) {
        viewModelScope.launch {
            repository.deleteHiddenPermanently(mediaId)
            _uiState.value = _uiState.value.copy(userMessage = "Permanently deleted")
        }
    }

    fun moveToTrash(item: MediaItem) {
        viewModelScope.launch {
            repository.moveToTrash(item)
            _uiState.value = _uiState.value.copy(
                activeViewerItem = null,
                userMessage = "Moved to Recently Deleted"
            )
        }
    }

    fun restoreFromTrash(mediaId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(mediaId)
            _uiState.value = _uiState.value.copy(userMessage = "Restored successfully")
        }
    }

    fun deleteTrashPermanently(mediaId: Long) {
        viewModelScope.launch {
            repository.permanentDeleteTrash(mediaId)
            _uiState.value = _uiState.value.copy(userMessage = "Permanently deleted")
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            _uiState.value = _uiState.value.copy(userMessage = "Trash emptied")
        }
    }

    fun shareMedia(item: MediaItem) {
        repository.shareMedia(item)
    }

    fun openVideoPlayer(item: MediaItem) {
        repository.openVideo(item)
    }

    fun clearUserMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }

    // Helper computation flows
    fun getFilteredMedia(): List<MediaItem> {
        val raw = _rawMedia.value
        val hiddenSet = hiddenIds.value.toSet()
        val trashSet = trashIds.value.toSet()
        val query = _uiState.value.searchQuery.trim().lowercase()

        var filtered = raw.filter { !hiddenSet.contains(it.id) && !trashSet.contains(it.id) }

        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.displayName.lowercase().contains(query) || it.albumName.lowercase().contains(query)
            }
        }

        return sortList(filtered, _uiState.value.sortOption)
    }

    fun getPhotosOnly(): List<MediaItem> {
        return getFilteredMedia().filter { !it.isVideo }
    }

    fun getVideosOnly(): List<MediaItem> {
        return getFilteredMedia().filter { it.isVideo }
    }

    fun getAlbums(): List<AlbumItem> {
        val visible = getFilteredMedia()
        return repository.groupIntoAlbums(visible)
    }

    fun getAlbumMedia(albumName: String): List<MediaItem> {
        val visible = getFilteredMedia()
        return visible.filter { it.albumName.equals(albumName, ignoreCase = true) }
    }

    fun getFavoriteMedia(): List<MediaItem> {
        val visible = getFilteredMedia()
        val favSet = favoriteIds.value.toSet()
        return visible.filter { favSet.contains(it.id) }
    }

    fun getTrashMedia(): List<MediaItem> {
        val entities = trashItems.value
        return entities.map {
            MediaItem(
                id = it.mediaId,
                uri = it.uriString.toUri(),
                displayName = it.displayName.ifEmpty { "Deleted_${it.mediaId}" },
                albumName = it.albumName.ifEmpty { "Trash" },
                dateTaken = it.dateDeleted,
                dateModified = it.dateDeleted,
                size = it.size,
                mimeType = it.mimeType,
                isVideo = it.isVideo,
                duration = it.duration,
                path = it.originalPath
            )
        }
    }

    fun getHiddenMedia(): List<MediaItem> {
        val entities = hiddenItems.value
        return entities.map {
            MediaItem(
                id = it.mediaId,
                uri = it.uriString.toUri(),
                displayName = it.displayName.ifEmpty { "Hidden_${it.mediaId}" },
                albumName = it.albumName.ifEmpty { "Hidden Vault" },
                dateTaken = it.dateHidden,
                dateModified = it.dateHidden,
                size = it.size,
                mimeType = it.mimeType,
                isVideo = it.isVideo,
                duration = it.duration,
                path = it.originalPath
            )
        }
    }

    private fun sortList(list: List<MediaItem>, sortOption: SortOption): List<MediaItem> {
        return when (sortOption) {
            SortOption.NEWEST -> list.sortedByDescending { if (it.dateTaken > 0) it.dateTaken else it.dateModified * 1000 }
            SortOption.OLDEST -> list.sortedBy { if (it.dateTaken > 0) it.dateTaken else it.dateModified * 1000 }
            SortOption.NAME -> list.sortedBy { it.displayName.lowercase() }
            SortOption.SIZE -> list.sortedByDescending { it.size }
        }
    }
}
