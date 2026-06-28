package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.HealthBatchRequest

class RetrofitHealthApi(
    private val service: HealthService,
) : HealthApi {

    override suspend fun uploadBatch(deviceToken: String, request: HealthBatchRequest) {
        service.uploadBatch("Bearer $deviceToken", request)
    }
}
