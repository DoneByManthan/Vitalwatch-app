package com.vitalwatch.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ──── Reading DTOs ────

data class ReadingDto(
    val id: String,
    @SerializedName("patient_id") val patientId: String,
    val timestamp: Long,
    @SerializedName("glucose_mgdl") val glucoseMgdl: Float,
    @SerializedName("systolic_mmhg") val systolicMmhg: Float,
    @SerializedName("diastolic_mmhg") val diastolicMmhg: Float,
    @SerializedName("heart_rate_bpm") val heartRateBpm: Float,
    @SerializedName("spo2_percent") val spo2Percent: Float,
    @SerializedName("signal_quality") val signalQuality: Int,
    @SerializedName("hrv_features") val hrvFeaturesJson: String,
    @SerializedName("created_at") val createdAt: Long
)

data class SyncReadingsRequest(
    val readings: List<ReadingDto>
)

data class SyncReadingsResponse(
    val synced: Int,
    val failed: Int,
    @SerializedName("failed_ids") val failedIds: List<String>
)

data class ReadingPageResponse(
    val readings: List<ReadingDto>,
    val total: Int,
    val page: Int,
    @SerializedName("page_size") val pageSize: Int
)

data class ReadingSummaryResponse(
    @SerializedName("avg_glucose") val avgGlucose: Float,
    @SerializedName("avg_systolic") val avgSystolic: Float,
    @SerializedName("avg_diastolic") val avgDiastolic: Float,
    @SerializedName("avg_hr") val avgHr: Float,
    @SerializedName("reading_count") val readingCount: Int,
    @SerializedName("period_days") val periodDays: Int
)
