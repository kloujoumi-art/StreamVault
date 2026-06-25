package com.streamvault.app.data.local.dao

import androidx.room.*
import com.streamvault.app.data.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE streamType = :type ORDER BY addedAt DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE streamId = :streamId AND streamType = :type)")
    fun isFavorite(streamId: Int, type: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE streamId = :streamId AND streamType = :type)")
    suspend fun isFavoriteOnce(streamId: Int, type: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE streamId = :streamId AND streamType = :type")
    suspend fun deleteFavorite(streamId: Int, type: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
}
