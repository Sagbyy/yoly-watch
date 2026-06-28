package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.HealthBatchRequest
import kotlinx.coroutines.delay

class MockHealthApi(
    private val responseDelayMillis: Long = 300L,
) : HealthApi {

    val uploaded = mutableListOf<HealthBatchRequest>()

    override suspend fun uploadBatch(deviceToken: String, request: HealthBatchRequest) {
        delay(responseDelayMillis)
        uploaded += request
    }
}
