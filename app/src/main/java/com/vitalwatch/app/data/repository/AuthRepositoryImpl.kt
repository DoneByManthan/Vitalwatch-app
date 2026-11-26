package com.vitalwatch.app.data.repository

import com.vitalwatch.app.data.local.dao.UserSessionDao
import com.vitalwatch.app.data.local.entity.UserSessionEntity
import com.vitalwatch.app.data.remote.api.AuthApi
import com.vitalwatch.app.data.remote.dto.LoginRequest
import com.vitalwatch.app.data.remote.dto.RegisterRequest
import com.vitalwatch.app.domain.model.User
import com.vitalwatch.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userSessionDao: UserSessionDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                val session = UserSessionEntity(
                    patientId = body.user.patientId,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenExpiry = System.currentTimeMillis() + body.expiresIn * 1000,
                    fullName = body.user.fullName,
                    email = body.user.email,
                    role = body.user.role
                )
                userSessionDao.insert(session)
                Result.success(session.toDomain())
            } else {
                val error = response.errorBody()?.string() ?: "Login failed"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, fullName: String): Result<User> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, fullName))
            if (response.isSuccessful) {
                val body = response.body()!!
                val session = UserSessionEntity(
                    patientId = body.user.patientId,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    tokenExpiry = System.currentTimeMillis() + body.expiresIn * 1000,
                    fullName = body.user.fullName,
                    email = body.user.email,
                    role = body.user.role
                )
                userSessionDao.insert(session)
                Result.success(session.toDomain())
            } else {
                val error = response.errorBody()?.string() ?: "Registration failed"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try { authApi.logout() } catch (_: Exception) {}
        userSessionDao.clearSession()
    }

    override suspend fun getCurrentUser(): User? {
        return userSessionDao.getSession()?.toDomain()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return userSessionDao.getSessionFlow().map { it != null }
    }

    private fun UserSessionEntity.toDomain() = User(
        patientId = patientId,
        fullName = fullName,
        email = email,
        role = role,
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenExpiry = tokenExpiry
    )
}
