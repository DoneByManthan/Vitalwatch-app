package com.vitalwatch.app.domain.usecase

import com.vitalwatch.app.domain.repository.SyncRepository
import javax.inject.Inject

class SyncReadingsUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<Int> = syncRepository.syncPendingReadings()
}
