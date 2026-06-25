package com.streamvault.app.data.local.dao

import androidx.room.*
import com.streamvault.app.data.local.entities.EpgProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {

    @Query("SELECT * FROM epg_programs WHERE streamId = :streamId ORDER BY startTimestamp ASC")
    fun getEpgForStream(streamId: Int): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs WHERE streamId = :streamId ORDER BY startTimestamp ASC")
    suspend fun getEpgForStreamOnce(streamId: Int): List<EpgProgramEntity>

    @Query("""
        SELECT * FROM epg_programs
        WHERE streamId = :streamId
        AND startTimestamp <= :now
        AND stopTimestamp >= :now
        LIMIT 1
    """)
    suspend fun getCurrentProgram(streamId: Int, now: Long = System.currentTimeMillis() / 1000): EpgProgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE streamId = :streamId")
    suspend fun clearForStream(streamId: Int)

    @Query("DELETE FROM epg_programs WHERE cachedAt < :cutoff")
    suspend fun clearExpired(cutoff: Long)

    @Query("SELECT cachedAt FROM epg_programs WHERE streamId = :streamId ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLastCacheTime(streamId: Int): Long?
}
