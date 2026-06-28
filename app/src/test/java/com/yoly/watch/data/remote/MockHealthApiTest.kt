package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.HealthBatchRequest
import com.yoly.watch.data.remote.dto.HealthSampleDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MockHealthApiTest {

    @Test
    fun `records every uploaded batch`() = runTest {
        val api = MockHealthApi(responseDelayMillis = 0)
        val request = HealthBatchRequest(
            watchId = "watch-1",
            samples = listOf(HealthSampleDto("HEART_RATE", 70, "2026-06-27T10:00:00Z")),
        )

        api.uploadBatch("dvc_token", request)

        assertEquals(listOf(request), api.uploaded)
    }
}
