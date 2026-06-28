package com.yoly.watch.data.health

import com.yoly.watch.domain.model.HeartRateSample
import com.yoly.watch.domain.model.StepCountSample
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.random.Random

class SimulatedHealthDataSourceTest {

    private val now: Instant = Instant.parse("2026-06-27T10:00:00Z")

    @Test
    fun `emits three heart rate samples and one step sample`() = runTest {
        val source = SimulatedHealthDataSource(now = { now }, random = Random(1))

        val samples = source.collect()

        assertEquals(3, samples.filterIsInstance<HeartRateSample>().size)
        assertEquals(1, samples.filterIsInstance<StepCountSample>().size)
    }

    @Test
    fun `heart rate samples stay within a plausible range`() = runTest {
        val source = SimulatedHealthDataSource(now = { now }, random = Random(1))

        val heartRates = source.collect().filterIsInstance<HeartRateSample>()

        assertTrue(heartRates.all { it.beatsPerMinute in 58..91 })
    }

    @Test
    fun `the latest heart rate sample is stamped at the current instant`() = runTest {
        val source = SimulatedHealthDataSource(now = { now }, random = Random(1))

        val heartRates = source.collect().filterIsInstance<HeartRateSample>()

        assertEquals(now, heartRates.last().recordedAt)
    }
}
