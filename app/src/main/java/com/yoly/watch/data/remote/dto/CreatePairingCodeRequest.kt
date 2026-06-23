package com.yoly.watch.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePairingCodeRequest(
    val deviceUuid: String,
)
