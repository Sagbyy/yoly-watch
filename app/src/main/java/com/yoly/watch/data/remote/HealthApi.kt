package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.HealthBatchRequest

interface HealthApi {
    suspend fun uploadBatch(deviceToken: String, request: HealthBatchRequest)
}
