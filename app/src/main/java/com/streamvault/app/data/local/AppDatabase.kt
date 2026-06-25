package com.streamvault.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.streamvault.app.data.local.dao.EpgDao
import com.streamvault.app.data.local.dao.FavoriteDao
import com.streamvault.app.data.local.dao.WatchHistoryDao
import com.streamvault.app.data.local.entities.EpgProgramEntity
import com.streamvault.app.data.local.entities.FavoriteEntity
import com.streamvault.app.data.local.entities.WatchHistoryEntity
import com.streamvault.app.utils.Constants

@Database(
    entities = [
        FavoriteEntity::class,
        WatchHistoryEntity::class,
        EpgProgramEntity::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun epgDao(): EpgDao
}
