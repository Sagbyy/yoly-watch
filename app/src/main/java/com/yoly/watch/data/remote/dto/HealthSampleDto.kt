package com.yoly.watch.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthSampleDto(
    val type: String,
    val value: Int,
    val recordedAt: String,
)

@Serializable
data class HealthBatchRequest(
    val watchId: String,
    val samples: List<HealthSampleDto>,
)
