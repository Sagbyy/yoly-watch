package com.yoly.watch.data.mapper

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingEventDto
import com.yoly.watch.domain.model.PairingEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class PairingCodeMapperTest {

    @Test
    fun `maps code dto to domain`() {
        val domain = PairingCodeDto("pid-1", "482915", 90).toDomain()
        assertEquals("pid-1", domain.pairingId)
        assertEquals("482915", domain.value)
        assertEquals(90, domain.validForSeconds)
    }

    @Test
    fun `maps confirmed event with its device token`() {
        val event = PairingEventDto("CONFIRMED", "dvc_123").toDomain()
        assertEquals(PairingEvent.Confirmed("dvc_123"), event)
    }

    @Test
    fun `maps confirmed without token to empty token`() {
        val event = PairingEventDto("CONFIRMED", null).toDomain()
        assertEquals(PairingEvent.Confirmed(""), event)
    }

    @Test
    fun `maps expired event`() {
        assertEquals(PairingEvent.Expired, PairingEventDto("EXPIRED").toDomain())
    }

    @Test
    fun `maps pending event`() {
        assertEquals(PairingEvent.Pending, PairingEventDto("PENDING").toDomain())
    }

    @Test
    fun `is case insensitive on status`() {
        assertEquals(PairingEvent.Expired, PairingEventDto("expired").toDomain())
    }

    @Test
    fun `maps unknown status to pending`() {
        assertEquals(PairingEvent.Pending, PairingEventDto("WHATEVER").toDomain())
    }
}
