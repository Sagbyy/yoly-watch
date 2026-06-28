package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.HealthBatchRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface HealthService {
    @POST("devices/health")
    suspend fun uploadBatch(
        @Header("Authorization") authorization: String,
        @Body request: HealthBatchRequest,
    )
}
