package com.vitalwatch.app.data.repository

import com.vitalwatch.app.data.local.dao.ReadingDao
import com.vitalwatch.app.data.local.dao.SyncQueueDao
import com.vitalwatch.app.data.local.entity.ReadingEntity
import com.vitalwatch.app.data.local.entity.SyncQueueEntity
import com.vitalwatch.app.domain.model.Reading
import com.vitalwatch.app.domain.model.SyncStatus
import com.vitalwatch.app.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepositoryImpl @Inject constructor(
    private val readingDao: ReadingDao,
    private val syncQueueDao: SyncQueueDao
) : ReadingRepository {

    override fun getAllReadings(): Flow<List<Reading>> {
        return readingDao.getAllReadings().map { list -> list.map { it.toDomain() } }
    }

    override fun getReadingsSince(timestamp: Long): Flow<List<Reading>> {
        return readingDao.getReadingsSince(timestamp).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveReading(reading: Reading) {
        val entity = reading.toEntity()
        readingDao.insert(entity)
        syncQueueDao.enqueue(SyncQueueEntity(readingId = reading.id))
    }

    override suspend fun getLatestReading(): Reading? {
        return readingDao.getLatestReading()?.toDomain()
    }

    override fun getPendingCount(): Flow<Int> = readingDao.getPendingCount()

    override suspend fun getPendingReadings(): List<Reading> {
        return readingDao.getPendingReadings().map { it.toDomain() }
    }

    private fun ReadingEntity.toDomain() = Reading(
        id = id,
        patientId = patientId,
        timestamp = timestamp,
        glucoseMgdl = glucoseMgdl,
        systolicMmhg = systolicMmhg,
        diastolicMmhg = diastolicMmhg,
        heartRateBpm = heartRateBpm,
        spo2Percent = spo2Percent,
        signalQuality = signalQuality,
        hrvFeaturesJson = hrvFeaturesJson,
        syncStatus = when (syncStatus) {
            "synced" -> SyncStatus.SYNCED
            "failed" -> SyncStatus.FAILED
            else -> SyncStatus.PENDING
        },
        syncedAt = syncedAt,
        createdAt = createdAt
    )

    private fun Reading.toEntity() = ReadingEntity(
        id = id,
        patientId = patientId,
        timestamp = timestamp,
        glucoseMgdl = glucoseMgdl,
        systolicMmhg = systolicMmhg,
        diastolicMmhg = diastolicMmhg,
        heartRateBpm = heartRateBpm,
        spo2Percent = spo2Percent,
        signalQuality = signalQuality,
        hrvFeaturesJson = hrvFeaturesJson,
        syncStatus = syncStatus.name.lowercase(),
        syncedAt = syncedAt,
        createdAt = createdAt
    )
}
