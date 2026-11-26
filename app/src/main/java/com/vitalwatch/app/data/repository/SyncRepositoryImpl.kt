package com.vitalwatch.app.data.repository

import com.vitalwatch.app.data.local.dao.ReadingDao
import com.vitalwatch.app.data.local.dao.SyncQueueDao
import com.vitalwatch.app.data.remote.api.ReadingApi
import com.vitalwatch.app.data.remote.dto.ReadingDto
import com.vitalwatch.app.data.remote.dto.SyncReadingsRequest
import com.vitalwatch.app.domain.repository.SyncRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val readingApi: ReadingApi,
    private val readingDao: ReadingDao,
    private val syncQueueDao: SyncQueueDao
) : SyncRepository {

    companion object {
        private const val BATCH_SIZE = 50
        private const val MAX_RETRIES = 5
    }

    override suspend fun syncPendingReadings(): Result<Int> {
        return try {
            // First mark exhausted items as failed
            val exhausted = syncQueueDao.getExhaustedItems(MAX_RETRIES)
            exhausted.forEach { item ->
                readingDao.updateSyncStatus(item.readingId, "failed", null)
                syncQueueDao.dequeue(item.readingId)
            }

            val pendingReadings = readingDao.getPendingReadings()
            if (pendingReadings.isEmpty()) return Result.success(0)

            var totalSynced = 0
            pendingReadings.chunked(BATCH_SIZE).forEach { batch ->
                val dtos = batch.map { entity ->
                    ReadingDto(
                        id = entity.id,
                        patientId = entity.patientId,
                        timestamp = entity.timestamp,
                        glucoseMgdl = entity.glucoseMgdl,
                        systolicMmhg = entity.systolicMmhg,
                        diastolicMmhg = entity.diastolicMmhg,
                        heartRateBpm = entity.heartRateBpm,
                        spo2Percent = entity.spo2Percent,
                        signalQuality = entity.signalQuality,
                        hrvFeaturesJson = entity.hrvFeaturesJson,
                        createdAt = entity.createdAt
                    )
                }
                val response = readingApi.syncReadings(SyncReadingsRequest(dtos))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val successIds = batch.map { it.id } - body.failedIds.toSet()
                    val now = System.currentTimeMillis()
                    readingDao.updateSyncStatusBatch(successIds, "synced")
                    successIds.forEach { id -> syncQueueDao.dequeue(id) }
                    body.failedIds.forEach { id ->
                        syncQueueDao.incrementRetry(id, now, "Server rejected")
                    }
                    totalSynced += body.synced
                } else {
                    val now = System.currentTimeMillis()
                    val error = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                    batch.forEach { entity ->
                        syncQueueDao.incrementRetry(entity.id, now, error)
                    }
                }
            }
            Result.success(totalSynced)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enqueuePendingReading(readingId: String) {
        syncQueueDao.enqueue(com.vitalwatch.app.data.local.entity.SyncQueueEntity(readingId = readingId))
    }
}
