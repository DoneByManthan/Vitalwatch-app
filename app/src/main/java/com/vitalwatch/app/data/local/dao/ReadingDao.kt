package com.vitalwatch.app.data.local.dao

import androidx.room.*
import com.vitalwatch.app.data.local.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: ReadingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<ReadingEntity>)

    @Update
    suspend fun update(reading: ReadingEntity)

    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE syncStatus = 'pending' ORDER BY createdAt ASC")
    suspend fun getPendingReadings(): List<ReadingEntity>

    @Query("SELECT * FROM readings WHERE patientId = :patientId ORDER BY timestamp DESC LIMIT :limit")
    fun getReadingsForPatient(patientId: String, limit: Int = 100): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getReadingsSince(since: Long): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE id = :id")
    suspend fun getReadingById(id: String): ReadingEntity?

    @Query("UPDATE readings SET syncStatus = :status, syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, syncedAt: Long?)

    @Query("UPDATE readings SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSyncStatusBatch(ids: List<String>, status: String)

    @Query("SELECT COUNT(*) FROM readings WHERE syncStatus = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM readings ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReading(): ReadingEntity?

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM readings WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    fun getReadingsInRange(start: Long, end: Long): Flow<List<ReadingEntity>>
}
