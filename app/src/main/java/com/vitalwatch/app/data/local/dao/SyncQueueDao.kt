package com.vitalwatch.app.data.local.dao

import androidx.room.*
import com.vitalwatch.app.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue ORDER BY id ASC LIMIT :limit")
    suspend fun getItems(limit: Int = 50): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getQueueSize(): Flow<Int>

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1, lastAttempt = :now, errorMsg = :error WHERE readingId = :readingId")
    suspend fun incrementRetry(readingId: String, now: Long, error: String?)

    @Query("DELETE FROM sync_queue WHERE readingId = :readingId")
    suspend fun dequeue(readingId: String)

    @Query("DELETE FROM sync_queue WHERE readingId IN (:readingIds)")
    suspend fun dequeueBatch(readingIds: List<String>)

    @Query("SELECT * FROM sync_queue WHERE retryCount > :threshold")
    suspend fun getExhaustedItems(threshold: Int = 5): List<SyncQueueEntity>
}
