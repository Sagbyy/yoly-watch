package com.yoly.watch.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PairingEventDto(
    val status: String,
    val deviceToken: String? = null,
)
