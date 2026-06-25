package com.streamvault.app.data.repository

import com.streamvault.app.data.local.dao.FavoriteDao
import com.streamvault.app.data.local.entities.FavoriteEntity
import com.streamvault.app.data.models.MediaItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>> =
        favoriteDao.getFavoritesByType(type)

    fun isFavorite(streamId: Int, type: String): Flow<Boolean> =
        favoriteDao.isFavorite(streamId, type)

    suspend fun toggleFavorite(item: MediaItem) {
        val isFav = favoriteDao.isFavoriteOnce(item.id, item.contentType.name.lowercase())
        if (isFav) {
            favoriteDao.deleteFavorite(item.id, item.contentType.name.lowercase())
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    streamId = item.id,
                    title = item.title,
                    thumbnailUrl = item.thumbnailUrl,
                    streamType = item.contentType.name.lowercase(),
                    streamUrl = item.streamUrl,
                    categoryId = item.categoryId,
                    containerExtension = item.containerExtension
                )
            )
        }
    }

    suspend fun addFavorite(entity: FavoriteEntity) = favoriteDao.insertFavorite(entity)

    suspend fun removeFavorite(streamId: Int, type: String) =
        favoriteDao.deleteFavorite(streamId, type)

    suspend fun clearAll() = favoriteDao.clearAllFavorites()
}
