package com.yoly.watch.domain.model

import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class HealthSampleTest {

    private val now: Instant = Instant.parse("2026-06-27T10:00:00Z")

    @Test
    fun `heart rate accepts a value within range`() {
        HeartRateSample(beatsPerMinute = 72, recordedAt = now)
    }

    @Test
    fun `heart rate rejects a value above 300`() {
        assertThrows(IllegalArgumentException::class.java) {
            HeartRateSample(beatsPerMinute = 301, recordedAt = now)
        }
    }

    @Test
    fun `heart rate rejects a negative value`() {
        assertThrows(IllegalArgumentException::class.java) {
            HeartRateSample(beatsPerMinute = -1, recordedAt = now)
        }
    }

    @Test
    fun `step count accepts zero`() {
        StepCountSample(count = 0, recordedAt = now)
    }

    @Test
    fun `step count rejects a negative value`() {
        assertThrows(IllegalArgumentException::class.java) {
            StepCountSample(count = -1, recordedAt = now)
        }
    }
}
