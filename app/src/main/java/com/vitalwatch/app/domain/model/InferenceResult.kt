package com.vitalwatch.app.domain.model

data class InferenceResult(
    val glucoseMgdl: Float,
    val systolicMmhg: Float,
    val diastolicMmhg: Float,
    val heartRateBpm: Float,
    val isReliable: Boolean,
    val confidenceScore: Float,
    val timestamp: Long
)
