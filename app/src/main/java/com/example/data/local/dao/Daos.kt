package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.HiddenEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.TrashEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY dateAdded DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT mediaId FROM favorites")
    fun getAllFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId)")
    fun isFavorite(mediaId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mediaId = :mediaId")
    suspend fun deleteFavorite(mediaId: Long)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}

@Dao
interface HiddenDao {
    @Query("SELECT * FROM hidden_items ORDER BY dateHidden DESC")
    fun getAllHidden(): Flow<List<HiddenEntity>>

    @Query("SELECT mediaId FROM hidden_items")
    fun getAllHiddenIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHidden(item: HiddenEntity)

    @Query("DELETE FROM hidden_items WHERE mediaId = :mediaId")
    suspend fun deleteHidden(mediaId: Long)

    @Query("DELETE FROM hidden_items")
    suspend fun clearHidden()
}

@Dao
interface TrashDao {
    @Query("SELECT * FROM trash_items ORDER BY dateDeleted DESC")
    fun getAllTrash(): Flow<List<TrashEntity>>

    @Query("SELECT mediaId FROM trash_items")
    fun getAllTrashIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrash(item: TrashEntity)

    @Query("DELETE FROM trash_items WHERE mediaId = :mediaId")
    suspend fun deleteTrash(mediaId: Long)

    @Query("DELETE FROM trash_items")
    suspend fun clearTrash()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)
}
