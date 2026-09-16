package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreRepository(private val context: Context) {

    private val TAG = "MediaStoreRepository"

    suspend fun loadAllMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaItem>()
        mediaList.addAll(loadImages())
        mediaList.addAll(loadVideos())
        // Sort initially by date taken/modified descending
        mediaList.sortedByDescending { if (it.dateTaken > 0) it.dateTaken else it.dateModified * 1000 }
    }

    suspend fun loadImages(): List<MediaItem> = withContext(Dispatchers.IO) {
        val images = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATA
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val bucketCol = it.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val dateTakenCol = it.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateModifiedCol = it.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                val sizeCol = it.getColumnIndex(MediaStore.Images.Media.SIZE)
                val mimeCol = it.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                val widthCol = it.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightCol = it.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val dataCol = it.getColumnIndex(MediaStore.Images.Media.DATA)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = if (nameCol != -1) it.getString(nameCol) ?: "IMG_$id" else "IMG_$id"
                    val bucketName = if (bucketCol != -1) it.getString(bucketCol) ?: "Pictures" else "Pictures"
                    val dateTaken = if (dateTakenCol != -1) it.getLong(dateTakenCol) else 0L
                    val dateModified = if (dateModifiedCol != -1) it.getLong(dateModifiedCol) else 0L
                    val size = if (sizeCol != -1) it.getLong(sizeCol) else 0L
                    val mimeType = if (mimeCol != -1) it.getString(mimeCol) ?: "image/*" else "image/*"
                    val width = if (widthCol != -1) it.getInt(widthCol) else 0
                    val height = if (heightCol != -1) it.getInt(heightCol) else 0
                    val path = if (dataCol != -1) it.getString(dataCol) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    images.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            albumName = bucketName.ifEmpty { "Pictures" },
                            dateTaken = dateTaken,
                            dateModified = dateModified,
                            size = size,
                            mimeType = mimeType,
                            isVideo = false,
                            width = width,
                            height = height,
                            path = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying images from MediaStore", e)
        }

        images
    }

    suspend fun loadVideos(): List<MediaItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = it.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val bucketCol = it.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val dateTakenCol = it.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
                val dateModifiedCol = it.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val sizeCol = it.getColumnIndex(MediaStore.Video.Media.SIZE)
                val mimeCol = it.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val durationCol = it.getColumnIndex(MediaStore.Video.Media.DURATION)
                val widthCol = it.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = it.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val dataCol = it.getColumnIndex(MediaStore.Video.Media.DATA)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = if (nameCol != -1) it.getString(nameCol) ?: "VID_$id" else "VID_$id"
                    val bucketName = if (bucketCol != -1) it.getString(bucketCol) ?: "Videos" else "Videos"
                    val dateTaken = if (dateTakenCol != -1) it.getLong(dateTakenCol) else 0L
                    val dateModified = if (dateModifiedCol != -1) it.getLong(dateModifiedCol) else 0L
                    val size = if (sizeCol != -1) it.getLong(sizeCol) else 0L
                    val mimeType = if (mimeCol != -1) it.getString(mimeCol) ?: "video/*" else "video/*"
                    val duration = if (durationCol != -1) it.getLong(durationCol) else 0L
                    val width = if (widthCol != -1) it.getInt(widthCol) else 0
                    val height = if (heightCol != -1) it.getInt(heightCol) else 0
                    val path = if (dataCol != -1) it.getString(dataCol) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    videos.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            albumName = bucketName.ifEmpty { "Videos" },
                            dateTaken = dateTaken,
                            dateModified = dateModified,
                            size = size,
                            mimeType = mimeType,
                            isVideo = true,
                            duration = duration,
                            width = width,
                            height = height,
                            path = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying videos from MediaStore", e)
        }

        videos
    }

    fun shareMedia(uri: Uri, isVideo: Boolean, title: String = "Share Media") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (isVideo) "video/*" else "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun openVideoPlayer(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch external video player", e)
        }
    }
}
