package com.vitalwatch.app.data.remote.api

import com.vitalwatch.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ReadingApi {

    @POST("api/v1/readings/sync")
    suspend fun syncReadings(@Body request: SyncReadingsRequest): Response<SyncReadingsResponse>

    @GET("api/v1/readings/")
    suspend fun getReadings(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<ReadingPageResponse>

    @GET("api/v1/readings/summary")
    suspend fun getSummary(
        @Query("days") days: Int = 7
    ): Response<ReadingSummaryResponse>
}
