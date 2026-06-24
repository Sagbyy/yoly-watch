package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingEvent
import com.yoly.watch.testutil.FakeDeviceCredentialsStore
import com.yoly.watch.testutil.FakePairingCodeRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCasesTest {

    @Test
    fun `request pairing code delegates to repository`() = runTest {
        val repo = FakePairingCodeRepository().apply {
            codeToReturn = PairingCode("pair-9", "654321", 60)
        }
        val result = RequestPairingCodeUseCase(repo)()
        assertEquals(repo.codeToReturn, result)
        assertEquals(1, repo.requestCount)
    }

    @Test
    fun `observe pairing events forwards the repository flow`() = runTest {
        val repo = FakePairingCodeRepository().apply {
            defaultEventFlow = flowOf(PairingEvent.Pending, PairingEvent.Confirmed("dvc"))
        }
        val events = ObservePairingEventsUseCase(repo)("pid-1").toList()
        assertEquals(listOf(PairingEvent.Pending, PairingEvent.Confirmed("dvc")), events)
        assertEquals(listOf("pid-1"), repo.observedPairingIds)
    }

    @Test
    fun `is watch paired is false without a token`() = runTest {
        val paired = IsWatchPairedUseCase(FakeDeviceCredentialsStore(token = null))()
        assertFalse(paired)
    }

    @Test
    fun `is watch paired is true with a token`() = runTest {
        val paired = IsWatchPairedUseCase(FakeDeviceCredentialsStore(token = "dvc_x"))()
        assertTrue(paired)
    }

    @Test
    fun `reset pairing clears the token`() = runTest {
        val store = FakeDeviceCredentialsStore(token = "dvc_x")
        ResetPairingUseCase(store)()
        assertNull(store.token)
    }
}
