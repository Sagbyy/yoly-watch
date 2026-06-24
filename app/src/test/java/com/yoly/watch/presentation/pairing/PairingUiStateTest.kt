package com.yoly.watch.presentation.pairing

import com.yoly.watch.domain.model.PairingCode
import org.junit.Assert.assertEquals
import org.junit.Test

class PairingUiStateTest {

    private fun success(remaining: Long, valid: Long) =
        PairingUiState.Success(PairingCode("p", "123456", valid), remaining)

    @Test
    fun `progress is the ratio of remaining over validity`() {
        assertEquals(0.5f, success(remaining = 60, valid = 120).progress)
    }

    @Test
    fun `progress is zero when nothing remains`() {
        assertEquals(0f, success(remaining = 0, valid = 120).progress)
    }

    @Test
    fun `progress is clamped to one`() {
        assertEquals(1f, success(remaining = 200, valid = 120).progress)
    }
}
