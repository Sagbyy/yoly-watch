package com.yoly.watch.presentation.pairing

import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingEvent
import com.yoly.watch.domain.usecase.IsWatchPairedUseCase
import com.yoly.watch.domain.usecase.ObservePairingEventsUseCase
import com.yoly.watch.domain.usecase.RequestPairingCodeUseCase
import com.yoly.watch.domain.usecase.ResetPairingUseCase
import com.yoly.watch.domain.usecase.SyncHealthDataUseCase
import com.yoly.watch.testutil.FakeDeviceCredentialsStore
import com.yoly.watch.testutil.FakeHealthRepository
import com.yoly.watch.testutil.FakePairingCodeRepository
import com.yoly.watch.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PairingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakePairingCodeRepository()
    private val store = FakeDeviceCredentialsStore()
    private val healthRepo = FakeHealthRepository()

    private fun createViewModel() = PairingViewModel(
        RequestPairingCodeUseCase(repo),
        ObservePairingEventsUseCase(repo),
        IsWatchPairedUseCase(store),
        ResetPairingUseCase(store),
        SyncHealthDataUseCase(healthRepo),
    )

    @Test
    fun `lands on home when a token is stored`() = runTest(mainDispatcherRule.dispatcher) {
        store.token = "dvc_existing"
        val vm = createViewModel()
        runCurrent()

        assertEquals(PairingUiState.Home, vm.uiState.value)
        assertEquals(0, repo.requestCount)
    }

    @Test
    fun `requests a code when not paired`() = runTest(mainDispatcherRule.dispatcher) {
        repo.codeToReturn = PairingCode("pair-1", "482915", 120)
        val vm = createViewModel()
        runCurrent()

        val state = vm.uiState.value
        assertTrue(state is PairingUiState.Success)
        assertEquals("482915", (state as PairingUiState.Success).code.value)
        assertEquals(120, state.remainingSeconds)
        assertEquals(1, repo.requestCount)
    }

    @Test
    fun `moves to confirmed on a confirmed event`() = runTest(mainDispatcherRule.dispatcher) {
        repo.defaultEventFlow = flowOf(PairingEvent.Confirmed("dvc"))
        val vm = createViewModel()
        runCurrent()

        assertEquals(PairingUiState.Confirmed, vm.uiState.value)
    }

    @Test
    fun `continue moves from confirmed to home`() = runTest(mainDispatcherRule.dispatcher) {
        repo.defaultEventFlow = flowOf(PairingEvent.Confirmed("dvc"))
        val vm = createViewModel()
        runCurrent()
        assertEquals(PairingUiState.Confirmed, vm.uiState.value)

        vm.goToHome()
        runCurrent()

        assertEquals(PairingUiState.Home, vm.uiState.value)
    }

    @Test
    fun `requests a new code on an expired event`() = runTest(mainDispatcherRule.dispatcher) {
        repo.enqueueEvents(flowOf(PairingEvent.Expired), emptyFlow())
        val vm = createViewModel()
        runCurrent()

        assertEquals(2, repo.requestCount)
        assertTrue(vm.uiState.value is PairingUiState.Success)
    }

    @Test
    fun `shows error when the request fails`() = runTest(mainDispatcherRule.dispatcher) {
        repo.errorToThrow = RuntimeException("network down")
        val vm = createViewModel()
        runCurrent()

        val state = vm.uiState.value
        assertTrue(state is PairingUiState.Error)
        assertEquals("network down", (state as PairingUiState.Error).message)
    }

    @Test
    fun `counts down the remaining seconds`() = runTest(mainDispatcherRule.dispatcher) {
        repo.codeToReturn = PairingCode("pair-1", "482915", 5)
        val vm = createViewModel()
        runCurrent()
        assertEquals(5, (vm.uiState.value as PairingUiState.Success).remainingSeconds)

        advanceTimeBy(1_100)
        runCurrent()
        assertEquals(4, (vm.uiState.value as PairingUiState.Success).remainingSeconds)
    }

    @Test
    fun `re pair clears the token and requests a fresh code`() = runTest(mainDispatcherRule.dispatcher) {
        store.token = "dvc_old"
        val vm = createViewModel()
        runCurrent()
        assertEquals(PairingUiState.Home, vm.uiState.value)

        vm.rePair()
        runCurrent()

        assertNull(store.token)
        assertTrue(vm.uiState.value is PairingUiState.Success)
    }

    @Test
    fun `sync now reports done with the uploaded count then returns to idle`() =
        runTest(mainDispatcherRule.dispatcher) {
            store.token = "dvc_existing"
            healthRepo.countToReturn = 4
            val vm = createViewModel()
            runCurrent()

            vm.syncNow()
            runCurrent()

            assertEquals(SyncUiState.Done(4), vm.syncStatus.value)
            assertEquals(1, healthRepo.syncCount)

            advanceTimeBy(2_100)
            runCurrent()
            assertEquals(SyncUiState.Idle, vm.syncStatus.value)
        }

    @Test
    fun `sync now reports error when the upload fails`() =
        runTest(mainDispatcherRule.dispatcher) {
            store.token = "dvc_existing"
            healthRepo.errorToThrow = RuntimeException("offline")
            val vm = createViewModel()
            runCurrent()

            vm.syncNow()
            runCurrent()

            assertEquals(SyncUiState.Error, vm.syncStatus.value)
        }
}
