package com.yoly.watch.data.remote.dto

data class PairingCodeDto(
    val code: String,
    val expiresInSeconds: Long,
)
