package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.PinkAccent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    mediaList: List<MediaItem>,
    initialIndex: Int,
    favoriteIds: Set<Long>,
    onBack: () -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    onShare: (MediaItem) -> Unit,
    onMoveToTrash: (MediaItem) -> Unit,
    onHideToVault: (MediaItem) -> Unit,
    onPlayVideo: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaList.isEmpty()) {
        onBack()
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, mediaList.size - 1),
        pageCount = { mediaList.size }
    )

    var showControls by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showTrashConfirm by remember { mutableStateOf(false) }
    var showHideConfirm by remember { mutableStateOf(false) }

    val currentItem = mediaList.getOrNull(pagerState.currentPage) ?: mediaList.first()
    val isCurrentFav = favoriteIds.contains(currentItem.id)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("photo_viewer_screen")
    ) {
        // Horizontal Pager for swipe left/right
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = mediaList[page]
            ZoomableMediaPage(
                item = item,
                onSingleTap = { showControls = !showControls },
                onPlayVideo = { onPlayVideo(item) }
            )
        }

        // Floating Top Toolbar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("viewer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = currentItem.displayName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pagerState.currentPage + 1} of ${mediaList.size}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Favorite Button
                        IconButton(
                            onClick = { onToggleFavorite(currentItem) },
                            modifier = Modifier.testTag("viewer_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isCurrentFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isCurrentFav) PinkAccent else Color.White
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = { onShare(currentItem) },
                            modifier = Modifier.testTag("viewer_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Floating Bottom Toolbar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Info Button
                    ViewerActionButton(
                        icon = Icons.Default.Info,
                        label = "Info",
                        onClick = { showInfoSheet = true },
                        testTag = "viewer_info_button"
                    )

                    // Hide to Vault Button
                    ViewerActionButton(
                        icon = Icons.Default.Lock,
                        label = "Hide",
                        onClick = { showHideConfirm = true },
                        testTag = "viewer_hide_button"
                    )

                    // Delete / Trash Button
                    ViewerActionButton(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        onClick = { showTrashConfirm = true },
                        tint = PinkAccent,
                        testTag = "viewer_delete_button"
                    )
                }
            }
        }
    }

    // Photo Details Bottom Sheet
    if (showInfoSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Photo Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                val dateFormatted = remember(currentItem.dateTaken, currentItem.dateModified) {
                    val timestamp = if (currentItem.dateTaken > 0) currentItem.dateTaken else currentItem.dateModified * 1000
                    if (timestamp > 0) {
                        SimpleDateFormat("EEE, MMM d, yyyy • hh:mm a", Locale.getDefault()).format(Date(timestamp))
                    } else "Unknown"
                }

                InfoRow(label = "File Name", value = currentItem.displayName)
                InfoRow(label = "Date Taken", value = dateFormatted)
                InfoRow(label = "File Size", value = currentItem.formattedSize)
                if (currentItem.width > 0 && currentItem.height > 0) {
                    InfoRow(label = "Resolution", value = "${currentItem.width} × ${currentItem.height}")
                }
                if (currentItem.isVideo && currentItem.duration > 0) {
                    InfoRow(label = "Duration", value = currentItem.formattedDuration)
                }
                InfoRow(label = "Folder / Album", value = currentItem.albumName)
                if (currentItem.path.isNotEmpty()) {
                    InfoRow(label = "Device Path", value = currentItem.path)
                }
                InfoRow(label = "MIME Type", value = currentItem.mimeType)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Move to Trash confirmation
    if (showTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showTrashConfirm = false },
            title = { Text("Move to Trash?") },
            text = { Text("This ${if (currentItem.isVideo) "video" else "photo"} will be moved to Recently Deleted. You can restore it within 30 days.") },
            confirmButton = {
                Button(
                    onClick = {
                        onMoveToTrash(currentItem)
                        showTrashConfirm = false
                    }
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrashConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Hide to Vault confirmation
    if (showHideConfirm) {
        AlertDialog(
            onDismissRequest = { showHideConfirm = false },
            title = { Text("Hide ${if (currentItem.isVideo) "Video" else "Photo"}?") },
            text = { Text("This item will be hidden from the normal gallery and protected inside your PIN-locked Hidden Vault.") },
            confirmButton = {
                Button(
                    onClick = {
                        onHideToVault(currentItem)
                        showHideConfirm = false
                    }
                ) {
                    Text("Hide to Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ZoomableMediaPage(
    item: MediaItem,
    onSingleTap: () -> Unit,
    onPlayVideo: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        if (scale > 1f) {
            val maxOffset = (scale - 1f) * 600f
            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffset, maxOffset),
                y = (offset.y + panChange.y).coerceIn(-maxOffset, maxOffset)
            )
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .transformable(state = transformState)
        )

        // Video Play Button Overlay
        if (item.isVideo) {
            IconButton(
                onClick = onPlayVideo,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("viewer_play_video_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircleFilled,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
private fun ViewerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
