package com.vitalwatch.app.domain.model

data class User(
    val patientId: String,
    val fullName: String,
    val email: String,
    val role: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenExpiry: Long
)
