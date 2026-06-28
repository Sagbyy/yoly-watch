package com.yoly.watch.domain.model

import java.time.Instant

sealed interface HealthSample {
    val recordedAt: Instant
}

data class HeartRateSample(
    val beatsPerMinute: Int,
    override val recordedAt: Instant,
) : HealthSample {
    init {
        require(beatsPerMinute in 0..300) {
            "Une fréquence cardiaque doit être comprise entre 0 et 300 bpm, reçu: $beatsPerMinute"
        }
    }
}

data class StepCountSample(
    val count: Int,
    override val recordedAt: Instant,
) : HealthSample {
    init {
        require(count >= 0) {
            "Un nombre de pas ne peut pas être négatif, reçu: $count"
        }
    }
}
