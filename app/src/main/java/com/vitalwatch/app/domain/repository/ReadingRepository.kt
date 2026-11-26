package com.vitalwatch.app.domain.repository

import com.vitalwatch.app.domain.model.Reading
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {
    fun getAllReadings(): Flow<List<Reading>>
    fun getReadingsSince(timestamp: Long): Flow<List<Reading>>
    suspend fun saveReading(reading: Reading)
    suspend fun getLatestReading(): Reading?
    fun getPendingCount(): Flow<Int>
    suspend fun getPendingReadings(): List<Reading>
}
