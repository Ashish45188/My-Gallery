package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.FavoritesDao
import com.example.data.local.dao.HiddenDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.TrashDao
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.HiddenEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.TrashEntity

@Database(
    entities = [
        FavoriteEntity::class,
        HiddenEntity::class,
        TrashEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun hiddenDao(): HiddenDao
    abstract fun trashDao(): TrashDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_gallery_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
