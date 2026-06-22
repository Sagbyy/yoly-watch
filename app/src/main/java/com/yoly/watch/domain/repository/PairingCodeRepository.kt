package com.yoly.watch.domain.repository

import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingStatus
import kotlinx.coroutines.flow.Flow

interface PairingCodeRepository {
    suspend fun requestPairingCode(): PairingCode
    fun observePairingStatus(pairingId: String): Flow<PairingStatus>
}
