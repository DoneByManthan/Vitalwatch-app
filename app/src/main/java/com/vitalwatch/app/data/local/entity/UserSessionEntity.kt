package com.vitalwatch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val patientId: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenExpiry: Long,
    val fullName: String,
    val email: String,
    val role: String
)
