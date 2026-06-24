package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.PairingEventDto
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockPairingCodeApiTest {

    private val api = MockPairingCodeApi(responseDelayMillis = 0, confirmAfterMillis = 0)

    @Test
    fun `returns a six digit numeric code`() = runTest {
        val dto = api.fetchPairingCode("android-1")
        assertEquals(6, dto.code.length)
        assertTrue(dto.code.all { it.isDigit() })
        assertEquals(120, dto.expiresInSeconds)
        assertTrue(dto.pairingId.contains("android-1"))
    }

    @Test
    fun `emits pending then confirmed`() = runTest {
        val events = api.observeEvents("pid").toList()
        assertEquals(
            listOf(PairingEventDto("PENDING"), PairingEventDto("CONFIRMED", "dvc_mock_token")),
            events,
        )
    }
}
