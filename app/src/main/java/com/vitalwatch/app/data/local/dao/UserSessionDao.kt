package com.vitalwatch.app.data.local.dao

import androidx.room.*
import com.vitalwatch.app.data.local.entity.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: UserSessionEntity)

    @Query("SELECT * FROM user_sessions WHERE id = 1 LIMIT 1")
    suspend fun getSession(): UserSessionEntity?

    @Query("SELECT * FROM user_sessions WHERE id = 1 LIMIT 1")
    fun getSessionFlow(): Flow<UserSessionEntity?>

    @Query("DELETE FROM user_sessions")
    suspend fun clearSession()

    @Query("UPDATE user_sessions SET accessToken = :token, tokenExpiry = :expiry WHERE id = 1")
    suspend fun updateAccessToken(token: String, expiry: Long)
}
