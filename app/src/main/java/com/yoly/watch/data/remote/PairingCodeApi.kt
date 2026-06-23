package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingEventDto
import kotlinx.coroutines.flow.Flow

interface PairingCodeApi {
    suspend fun fetchPairingCode(deviceUuid: String): PairingCodeDto
    fun observeEvents(pairingId: String): Flow<PairingEventDto>
}
