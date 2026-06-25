package com.atilfaz.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_programs")
data class EpgProgramEntity(
    @PrimaryKey val id: String,
    val streamId: Int,
    val title: String,
    val description: String,
    val startTimestamp: Long,
    val stopTimestamp: Long,
    val channelId: String = "",
    val cachedAt: Long = System.currentTimeMillis()
)
