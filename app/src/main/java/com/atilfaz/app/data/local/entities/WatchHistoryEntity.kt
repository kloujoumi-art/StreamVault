package com.atilfaz.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val streamId: Int,
    val title: String,
    val thumbnailUrl: String,
    val streamType: String,
    val streamUrl: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressMs: Long = 0L,
    val durationMs: Long = 0L
)
