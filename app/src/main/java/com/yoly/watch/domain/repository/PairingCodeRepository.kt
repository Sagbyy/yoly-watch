package com.yoly.watch.domain.repository

import com.yoly.watch.domain.model.PairingCode

interface PairingCodeRepository {
    suspend fun requestPairingCode(): PairingCode
}
