package com.yoly.watch.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PairingCodeDto(
    val pairingId: String,
    val code: String,
    val expiresInSeconds: Long,
)
