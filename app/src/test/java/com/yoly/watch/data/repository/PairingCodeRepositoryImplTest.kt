package com.yoly.watch.data.repository

import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingEventDto
import com.yoly.watch.domain.model.PairingEvent
import com.yoly.watch.testutil.FakeDeviceCredentialsStore
import com.yoly.watch.testutil.FakePairingCodeApi
import com.yoly.watch.testutil.FakeWatchIdentityProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class PairingCodeRepositoryImplTest {

    @Test
    fun `request pairing code sends the android id and maps the response`() = runTest {
        val api = FakePairingCodeApi(codeDto = PairingCodeDto("pid", "112233", 120))
        val repo = PairingCodeRepositoryImpl(
            api,
            FakeWatchIdentityProvider("android-42"),
            FakeDeviceCredentialsStore(),
        )

        val code = repo.requestPairingCode()

        assertEquals("android-42", api.lastAndroidId)
        assertEquals("pid", code.pairingId)
        assertEquals("112233", code.value)
        assertEquals(120, code.validForSeconds)
    }

    @Test
    fun `persists the device token on a confirmed event`() = runTest {
        val store = FakeDeviceCredentialsStore()
        val api = FakePairingCodeApi(events = flowOf(PairingEventDto("CONFIRMED", "dvc_saved")))
        val repo = PairingCodeRepositoryImpl(api, FakeWatchIdentityProvider(), store)

        val events = repo.observePairingEvents("pid").toList()

        assertEquals(listOf(PairingEvent.Confirmed("dvc_saved")), events)
        assertEquals("dvc_saved", store.token)
    }

    @Test
    fun `does not persist anything for a pending event`() = runTest {
        val store = FakeDeviceCredentialsStore()
        val api = FakePairingCodeApi(events = flowOf(PairingEventDto("PENDING")))
        val repo = PairingCodeRepositoryImpl(api, FakeWatchIdentityProvider(), store)

        repo.observePairingEvents("pid").toList()

        assertEquals(null, store.token)
    }

    @Test
    fun `reconnects after a stream failure`() = runTest {
        val store = FakeDeviceCredentialsStore()
        val flakyApi = object : PairingCodeApi {
            var attempts = 0
            override suspend fun fetchPairingCode(androidId: String) = error("unused")
            override fun observeEvents(pairingId: String): Flow<PairingEventDto> = flow {
                attempts++
                if (attempts == 1) throw IOException("dropped")
                emit(PairingEventDto("CONFIRMED", "dvc_after_retry"))
            }
        }
        val repo = PairingCodeRepositoryImpl(flakyApi, FakeWatchIdentityProvider(), store)

        val events = repo.observePairingEvents("pid").toList()

        assertEquals(listOf(PairingEvent.Confirmed("dvc_after_retry")), events)
        assertEquals(2, flakyApi.attempts)
        assertEquals("dvc_after_retry", store.token)
    }
}
