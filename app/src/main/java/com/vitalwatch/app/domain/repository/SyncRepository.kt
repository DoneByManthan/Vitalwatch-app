package com.vitalwatch.app.domain.repository

interface SyncRepository {
    suspend fun syncPendingReadings(): Result<Int>
    suspend fun enqueuePendingReading(readingId: String)
}
