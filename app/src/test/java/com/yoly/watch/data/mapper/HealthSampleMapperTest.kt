package com.yoly.watch.data.mapper

import com.yoly.watch.domain.model.HeartRateSample
import com.yoly.watch.domain.model.StepCountSample
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class HealthSampleMapperTest {

    private val recordedAt: Instant = Instant.parse("2026-06-27T10:00:00Z")

    @Test
    fun `maps a heart rate sample to its dto`() {
        val dto = HeartRateSample(beatsPerMinute = 81, recordedAt = recordedAt).toDto()

        assertEquals("HEART_RATE", dto.type)
        assertEquals(81, dto.value)
        assertEquals("2026-06-27T10:00:00Z", dto.recordedAt)
    }

    @Test
    fun `maps a step count sample to its dto`() {
        val dto = StepCountSample(count = 42, recordedAt = recordedAt).toDto()

        assertEquals("STEPS", dto.type)
        assertEquals(42, dto.value)
        assertEquals("2026-06-27T10:00:00Z", dto.recordedAt)
    }
}
