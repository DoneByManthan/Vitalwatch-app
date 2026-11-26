package com.vitalwatch.app.data.remote.interceptor

import com.vitalwatch.app.data.local.dao.UserSessionDao
import com.vitalwatch.app.data.remote.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val userSessionDao: UserSessionDao,
    // Use Provider to avoid circular dependency since AuthApi needs this interceptor
    private val authApiProvider: Provider<retrofit2.Retrofit>
) : Interceptor {

    companion object {
        private const val HTTP_UNAUTHORIZED = 401
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == HTTP_UNAUTHORIZED) {
            response.close()
            val refreshed = runBlocking { tryRefreshToken() }
            if (refreshed) {
                val newToken = runBlocking { userSessionDao.getSession()?.accessToken }
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }
        return response
    }

    private suspend fun tryRefreshToken(): Boolean {
        return try {
            val session = userSessionDao.getSession() ?: return false
            val retrofit = authApiProvider.get()
            val authApi = retrofit.create(com.vitalwatch.app.data.remote.api.AuthApi::class.java)
            val result = authApi.refresh(RefreshRequest(session.refreshToken))
            if (result.isSuccessful) {
                val body = result.body()!!
                userSessionDao.updateAccessToken(
                    token = body.accessToken,
                    expiry = System.currentTimeMillis() + (body.expiresIn * 1000)
                )
                true
            } else {
                userSessionDao.clearSession()
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
