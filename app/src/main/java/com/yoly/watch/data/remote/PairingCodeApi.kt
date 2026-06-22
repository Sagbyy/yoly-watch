package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingStatusDto
import kotlinx.coroutines.flow.Flow

interface PairingCodeApi {
    suspend fun fetchPairingCode(watchId: String): PairingCodeDto
    fun observeStatus(pairingId: String): Flow<PairingStatusDto>
}
