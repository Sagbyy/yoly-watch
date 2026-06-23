package com.yoly.watch.domain.repository

import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingEvent
import kotlinx.coroutines.flow.Flow

interface PairingCodeRepository {
    suspend fun requestPairingCode(): PairingCode
    fun observePairingEvents(pairingId: String): Flow<PairingEvent>
}
