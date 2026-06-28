package com.yoly.watch.data.mapper

import com.yoly.watch.data.remote.dto.HealthSampleDto
import com.yoly.watch.domain.model.HealthSample
import com.yoly.watch.domain.model.HeartRateSample
import com.yoly.watch.domain.model.StepCountSample

fun HealthSample.toDto(): HealthSampleDto = when (this) {
    is HeartRateSample -> HealthSampleDto(
        type = "HEART_RATE",
        value = beatsPerMinute,
        recordedAt = recordedAt.toString(),
    )
    is StepCountSample -> HealthSampleDto(
        type = "STEPS",
        value = count,
        recordedAt = recordedAt.toString(),
    )
}
