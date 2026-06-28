package com.yoly.watch.testutil

import com.yoly.watch.data.remote.HealthApi
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.data.remote.dto.HealthBatchRequest
import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingEventDto
import com.yoly.watch.domain.health.HealthDataSource
import com.yoly.watch.domain.identity.DeviceCredentialsStore
import com.yoly.watch.domain.identity.WatchIdentityProvider
import com.yoly.watch.domain.model.HealthSample
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingEvent
import com.yoly.watch.domain.repository.HealthRepository
import com.yoly.watch.domain.repository.PairingCodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeWatchIdentityProvider(private val id: String = "android-id") : WatchIdentityProvider {
    override suspend fun watchId(): String = id
}

class FakeDeviceCredentialsStore(var token: String? = null) : DeviceCredentialsStore {
    override suspend fun saveDeviceToken(token: String) {
        this.token = token
    }

    override suspend fun deviceToken(): String? = token

    override suspend fun clearDeviceToken() {
        token = null
    }
}

class FakePairingCodeApi(
    private val codeDto: PairingCodeDto = PairingCodeDto("pid", "123456", 120),
    private val events: Flow<PairingEventDto> = emptyFlow(),
) : PairingCodeApi {
    var lastAndroidId: String? = null

    override suspend fun fetchPairingCode(androidId: String): PairingCodeDto {
        lastAndroidId = androidId
        return codeDto
    }

    override fun observeEvents(pairingId: String): Flow<PairingEventDto> = events
}

class FakeHealthDataSource(
    var samples: List<HealthSample> = emptyList(),
) : HealthDataSource {
    var collectCount = 0

    override suspend fun collect(): List<HealthSample> {
        collectCount++
        return samples
    }
}

class FakeHealthApi(var errorToThrow: Throwable? = null) : HealthApi {
    val uploaded = mutableListOf<HealthBatchRequest>()
    var lastToken: String? = null

    override suspend fun uploadBatch(deviceToken: String, request: HealthBatchRequest) {
        errorToThrow?.let { throw it }
        lastToken = deviceToken
        uploaded += request
    }
}

class FakeHealthRepository(
    var countToReturn: Int = 0,
    var errorToThrow: Throwable? = null,
) : HealthRepository {
    var syncCount = 0

    override suspend fun syncNow(): Int {
        syncCount++
        errorToThrow?.let { throw it }
        return countToReturn
    }
}

class FakePairingCodeRepository : PairingCodeRepository {
    var codeToReturn: PairingCode = PairingCode("pair-1", "123456", 120)
    var errorToThrow: Throwable? = null
    var requestCount = 0

    private val eventFlows = ArrayDeque<Flow<PairingEvent>>()
    var defaultEventFlow: Flow<PairingEvent> = emptyFlow()
    val observedPairingIds = mutableListOf<String>()

    fun enqueueEvents(vararg flows: Flow<PairingEvent>) {
        eventFlows.addAll(flows)
    }

    override suspend fun requestPairingCode(): PairingCode {
        requestCount++
        errorToThrow?.let { throw it }
        return codeToReturn
    }

    override fun observePairingEvents(pairingId: String): Flow<PairingEvent> {
        observedPairingIds += pairingId
        return if (eventFlows.isNotEmpty()) eventFlows.removeFirst() else defaultEventFlow
    }
}
