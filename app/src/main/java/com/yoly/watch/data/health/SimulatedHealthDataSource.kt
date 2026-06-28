package com.yoly.watch.data.health

import com.yoly.watch.domain.health.HealthDataSource
import com.yoly.watch.domain.model.HealthSample
import com.yoly.watch.domain.model.HeartRateSample
import com.yoly.watch.domain.model.StepCountSample
import java.time.Instant
import kotlin.random.Random

class SimulatedHealthDataSource(
    private val now: () -> Instant = Instant::now,
    private val random: Random = Random.Default,
) : HealthDataSource {

    override suspend fun collect(): List<HealthSample> {
        val timestamp = now()
        val heartRates = (0 until HEART_RATE_SAMPLES).map { index ->
            HeartRateSample(
                beatsPerMinute = random.nextInt(MIN_BPM, MAX_BPM),
                recordedAt = timestamp.minusSeconds((HEART_RATE_SAMPLES - 1 - index) * SAMPLE_SPACING_SECONDS),
            )
        }
        val steps = StepCountSample(
            count = random.nextInt(0, MAX_STEPS_PER_CYCLE),
            recordedAt = timestamp,
        )
        return heartRates + steps
    }

    private companion object {
        const val HEART_RATE_SAMPLES = 3
        const val SAMPLE_SPACING_SECONDS = 20L
        const val MIN_BPM = 58
        const val MAX_BPM = 92
        const val MAX_STEPS_PER_CYCLE = 50
    }
}
