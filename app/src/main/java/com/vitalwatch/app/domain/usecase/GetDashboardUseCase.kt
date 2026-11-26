package com.vitalwatch.app.domain.usecase

import com.vitalwatch.app.domain.model.Reading
import com.vitalwatch.app.domain.repository.AuthRepository
import com.vitalwatch.app.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class DashboardData(
    val latestReading: Reading?,
    val recentReadings: List<Reading>,
    val pendingCount: Int,
    val userName: String
)

class GetDashboardUseCase @Inject constructor(
    private val readingRepository: ReadingRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        val since = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        return combine(
            readingRepository.getReadingsSince(since),
            readingRepository.getPendingCount()
        ) { readings, pending ->
            DashboardData(
                latestReading = readings.firstOrNull(),
                recentReadings = readings.take(50),
                pendingCount = pending,
                userName = authRepository.getCurrentUser()?.fullName ?: "User"
            )
        }
    }
}
