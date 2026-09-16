package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ThemeOption
import com.example.ui.components.GalleryBottomNav
import com.example.ui.components.GalleryHeader
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HiddenVaultScreen
import com.example.ui.screens.PermissionScreen
import com.example.ui.screens.PhotoViewerScreen
import com.example.ui.screens.PhotosScreen
import com.example.ui.screens.RecentlyDeletedScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@Composable
fun GalleryApp(
    viewModel: GalleryViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteIdsList by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val favoriteIds = remember(favoriteIdsList) { favoriteIdsList.toSet() }
    val trashList = remember(uiState, viewModel.trashItems.collectAsStateWithLifecycle().value) { viewModel.getTrashMedia() }
    val hiddenList = remember(uiState, viewModel.hiddenItems.collectAsStateWithLifecycle().value) { viewModel.getHiddenMedia() }

    val snackbarHostState = remember { SnackbarHostState() }

    // Required permissions based on Android API level
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        if (granted) {
            viewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            viewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Determine Theme Mode
    val isDark = when (uiState.themeOption) {
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
    }

    MyApplicationTheme(darkTheme = isDark) {
        if (!uiState.hasPermission) {
            PermissionScreen(
                onRequestPermission = {
                    permissionLauncher.launch(requiredPermissions)
                }
            )
            return@MyApplicationTheme
        }

        // Full Screen Overlays: Photo Viewer, Settings, Recently Deleted, Hidden Vault
        when {
            uiState.activeViewerItem != null -> {
                val activeList = remember(uiState.selectedTab, uiState.selectedAlbumName) {
                    when {
                        uiState.selectedAlbumName != null -> viewModel.getAlbumMedia(uiState.selectedAlbumName!!)
                        uiState.selectedTab == GalleryTab.VIDEOS -> viewModel.getVideosOnly()
                        uiState.selectedTab == GalleryTab.FAVORITES -> viewModel.getFavoriteMedia()
                        else -> viewModel.getFilteredMedia()
                    }
                }

                PhotoViewerScreen(
                    mediaList = activeList,
                    initialIndex = uiState.currentViewerIndex,
                    favoriteIds = favoriteIds,
                    onBack = { viewModel.closeViewer() },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onShare = { viewModel.shareMedia(it) },
                    onMoveToTrash = { viewModel.moveToTrash(it) },
                    onHideToVault = { viewModel.hideItem(it) },
                    onPlayVideo = { viewModel.openVideoPlayer(it) }
                )
            }

            uiState.isSettingsOpen -> {
                SettingsScreen(
                    currentTheme = uiState.themeOption,
                    gridColumns = uiState.gridColumns,
                    currentSort = uiState.sortOption,
                    isPinSet = uiState.isPinSet,
                    onBack = { viewModel.closeSettings() },
                    onSelectTheme = { viewModel.setThemeOption(it) },
                    onSelectGridColumns = { viewModel.setGridColumns(it) },
                    onSelectSort = { viewModel.setSortOption(it) },
                    onSetPin = { viewModel.setVaultPin(it) }
                )
            }

            uiState.isRecentlyDeletedOpen -> {
                RecentlyDeletedScreen(
                    trashList = trashList,
                    gridColumns = uiState.gridColumns,
                    onBack = { viewModel.closeRecentlyDeleted() },
                    onRestoreItem = { viewModel.restoreFromTrash(it) },
                    onPermanentDeleteItem = { viewModel.deleteTrashPermanently(it) },
                    onEmptyTrash = { viewModel.emptyTrash() }
                )
            }

            uiState.isHiddenVaultOpen -> {
                HiddenVaultScreen(
                    isUnlocked = uiState.isHiddenVaultUnlocked,
                    isPinSet = uiState.isPinSet,
                    hiddenList = hiddenList,
                    gridColumns = uiState.gridColumns,
                    onBack = { viewModel.closeHiddenVault() },
                    onUnlock = { viewModel.unlockVault(it) },
                    onSetPin = { viewModel.setVaultPin(it) },
                    onLock = { viewModel.lockVault() },
                    onUnhideItem = { viewModel.unhideItem(it) },
                    onDeleteItem = { viewModel.deleteHiddenPermanently(it) }
                )
            }

            uiState.selectedAlbumName != null -> {
                val albumPhotos = viewModel.getAlbumMedia(uiState.selectedAlbumName!!)
                AlbumDetailScreen(
                    albumName = uiState.selectedAlbumName!!,
                    mediaList = albumPhotos,
                    favoriteIds = favoriteIds,
                    gridColumns = uiState.gridColumns,
                    onBack = { viewModel.closeAlbum() },
                    onPhotoClick = { viewModel.openViewer(it, albumPhotos) }
                )
            }

            else -> {
                // Main Gallery Shell
                Scaffold(
                    topBar = {
                        GalleryHeader(
                            searchQuery = uiState.searchQuery,
                            isSearchOpen = uiState.isSearchOpen,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onToggleSearch = { viewModel.toggleSearch() },
                            currentSort = uiState.sortOption,
                            onSelectSort = { viewModel.setSortOption(it) },
                            onOpenTrash = { viewModel.openRecentlyDeleted() },
                            onOpenVault = { viewModel.openHiddenVault() },
                            onOpenSettings = { viewModel.openSettings() }
                        )
                    },
                    bottomBar = {
                        GalleryBottomNav(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = uiState.selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "gallery_tab_content"
                        ) { tab ->
                            when (tab) {
                                GalleryTab.PHOTOS -> {
                                    val photos = viewModel.getFilteredMedia()
                                    PhotosScreen(
                                        mediaList = photos,
                                        favoriteIds = favoriteIds,
                                        gridColumns = uiState.gridColumns,
                                        trashCount = trashList.size,
                                        hiddenCount = hiddenList.size,
                                        onPhotoClick = { viewModel.openViewer(it, photos) },
                                        onOpenTrash = { viewModel.openRecentlyDeleted() },
                                        onOpenVault = { viewModel.openHiddenVault() }
                                    )
                                }

                                GalleryTab.ALBUMS -> {
                                    val albums = viewModel.getAlbums()
                                    AlbumsScreen(
                                        albums = albums,
                                        onAlbumClick = { viewModel.openAlbum(it) }
                                    )
                                }

                                GalleryTab.VIDEOS -> {
                                    val videos = viewModel.getVideosOnly()
                                    VideosScreen(
                                        videoList = videos,
                                        favoriteIds = favoriteIds,
                                        gridColumns = uiState.gridColumns,
                                        onVideoClick = { viewModel.openViewer(it, videos) }
                                    )
                                }

                                GalleryTab.FAVORITES -> {
                                    val favs = viewModel.getFavoriteMedia()
                                    FavoritesScreen(
                                        favoriteMediaList = favs,
                                        favoriteIds = favoriteIds,
                                        gridColumns = uiState.gridColumns,
                                        onPhotoClick = { viewModel.openViewer(it, favs) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
