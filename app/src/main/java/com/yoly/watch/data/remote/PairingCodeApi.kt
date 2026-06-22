package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.PairingCodeDto

interface PairingCodeApi {
    suspend fun fetchPairingCode(): PairingCodeDto
}
