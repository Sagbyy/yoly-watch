package com.yoly.watch.data.repository

import com.yoly.watch.data.mapper.toDto
import com.yoly.watch.data.remote.HealthApi
import com.yoly.watch.data.remote.dto.HealthBatchRequest
import com.yoly.watch.domain.health.HealthDataSource
import com.yoly.watch.domain.identity.DeviceCredentialsStore
import com.yoly.watch.domain.identity.WatchIdentityProvider
import com.yoly.watch.domain.repository.HealthRepository

class HealthRepositoryImpl(
    private val dataSource: HealthDataSource,
    private val api: HealthApi,
    private val credentialsStore: DeviceCredentialsStore,
    private val watchIdentityProvider: WatchIdentityProvider,
) : HealthRepository {

    override suspend fun syncNow(): Int {
        val token = credentialsStore.deviceToken() ?: return 0
        val samples = dataSource.collect()
        if (samples.isEmpty()) return 0
        api.uploadBatch(
            deviceToken = token,
            request = HealthBatchRequest(
                watchId = watchIdentityProvider.watchId(),
                samples = samples.map { it.toDto() },
            ),
        )
        return samples.size
    }
}
