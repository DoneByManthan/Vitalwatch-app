package com.vitalwatch.app.domain.model

data class Reading(
    val id: String,
    val patientId: String,
    val timestamp: Long,
    val glucoseMgdl: Float,
    val systolicMmhg: Float,
    val diastolicMmhg: Float,
    val heartRateBpm: Float,
    val spo2Percent: Float,
    val signalQuality: Int,
    val hrvFeaturesJson: String,
    val syncStatus: SyncStatus,
    val syncedAt: Long?,
    val createdAt: Long
) {
    val glucoseStatus: GlucoseStatus
        get() = when {
            glucoseMgdl < 70f -> GlucoseStatus.LOW
            glucoseMgdl <= 180f -> GlucoseStatus.NORMAL
            glucoseMgdl <= 300f -> GlucoseStatus.HIGH
            else -> GlucoseStatus.CRITICAL
        }
}

enum class SyncStatus { PENDING, SYNCED, FAILED }

enum class GlucoseStatus { LOW, NORMAL, HIGH, CRITICAL }
