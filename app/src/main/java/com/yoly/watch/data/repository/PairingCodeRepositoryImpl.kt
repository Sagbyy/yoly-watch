package com.yoly.watch.data.repository

import com.yoly.watch.data.mapper.toDomain
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.domain.identity.WatchIdentityProvider
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingStatus
import com.yoly.watch.domain.repository.PairingCodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PairingCodeRepositoryImpl(
    private val api: PairingCodeApi,
    private val watchIdentityProvider: WatchIdentityProvider,
) : PairingCodeRepository {

    override suspend fun requestPairingCode(): PairingCode =
        api.fetchPairingCode(watchIdentityProvider.watchId()).toDomain()

    override fun observePairingStatus(pairingId: String): Flow<PairingStatus> =
        api.observeStatus(pairingId).map { it.toDomain() }
}
