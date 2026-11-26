package com.vitalwatch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val timestamp: Long,
    val glucoseMgdl: Float,
    val systolicMmhg: Float,
    val diastolicMmhg: Float,
    val heartRateBpm: Float,
    val spo2Percent: Float,
    val signalQuality: Int,
    val hrvFeaturesJson: String,
    val syncStatus: String = "pending",
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
