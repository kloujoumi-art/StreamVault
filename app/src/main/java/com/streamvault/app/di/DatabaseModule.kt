package com.streamvault.app.di

import android.content.Context
import androidx.room.Room
import com.streamvault.app.data.local.AppDatabase
import com.streamvault.app.data.local.dao.EpgDao
import com.streamvault.app.data.local.dao.FavoriteDao
import com.streamvault.app.data.local.dao.WatchHistoryDao
import com.streamvault.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    @Singleton
    fun provideWatchHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    @Singleton
    fun provideEpgDao(db: AppDatabase): EpgDao = db.epgDao()
}
