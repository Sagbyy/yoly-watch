package com.yoly.watch.data.repository

import com.yoly.watch.data.mapper.toDomain
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.domain.identity.DeviceCredentialsStore
import com.yoly.watch.domain.identity.WatchIdentityProvider
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingEvent
import com.yoly.watch.domain.repository.PairingCodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class PairingCodeRepositoryImpl(
    private val api: PairingCodeApi,
    private val watchIdentityProvider: WatchIdentityProvider,
    private val credentialsStore: DeviceCredentialsStore,
) : PairingCodeRepository {

    override suspend fun requestPairingCode(): PairingCode =
        api.fetchPairingCode(watchIdentityProvider.watchId()).toDomain()

    override fun observePairingEvents(pairingId: String): Flow<PairingEvent> =
        api.observeEvents(pairingId)
            .map { it.toDomain() }
            .onEach { event ->
                if (event is PairingEvent.Confirmed && event.deviceToken.isNotEmpty()) {
                    credentialsStore.saveDeviceToken(event.deviceToken)
                }
            }
}
