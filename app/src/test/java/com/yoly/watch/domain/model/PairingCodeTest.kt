package com.yoly.watch.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PairingCodeTest {

    @Test
    fun `accepts a valid six digit code`() {
        val code = PairingCode("pair-1", "482915", 120)
        assertEquals("482915", code.value)
        assertEquals(120, code.validForSeconds)
    }

    @Test
    fun `rejects a code shorter than six digits`() {
        assertThrows(IllegalArgumentException::class.java) {
            PairingCode("pair-1", "48291", 120)
        }
    }

    @Test
    fun `rejects a code longer than six digits`() {
        assertThrows(IllegalArgumentException::class.java) {
            PairingCode("pair-1", "4829151", 120)
        }
    }

    @Test
    fun `rejects a non numeric code`() {
        assertThrows(IllegalArgumentException::class.java) {
            PairingCode("pair-1", "48a915", 120)
        }
    }

    @Test
    fun `rejects a non positive validity`() {
        assertThrows(IllegalArgumentException::class.java) {
            PairingCode("pair-1", "482915", 0)
        }
    }
}
