package com.atilfaz.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val streamId: Int,
    val title: String,
    val thumbnailUrl: String,
    val streamType: String,
    val streamUrl: String,
    val categoryId: String = "",
    val containerExtension: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
