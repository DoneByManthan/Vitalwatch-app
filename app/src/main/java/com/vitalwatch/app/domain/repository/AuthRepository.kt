package com.vitalwatch.app.domain.repository

import com.vitalwatch.app.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, fullName: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun isLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean>
}
