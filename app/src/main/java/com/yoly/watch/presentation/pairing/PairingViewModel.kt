package com.yoly.watch.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yoly.watch.di.ServiceLocator
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingEvent
import com.yoly.watch.domain.usecase.IsWatchPairedUseCase
import com.yoly.watch.domain.usecase.ObservePairingEventsUseCase
import com.yoly.watch.domain.usecase.RequestPairingCodeUseCase
import com.yoly.watch.domain.usecase.ResetPairingUseCase
import com.yoly.watch.domain.usecase.SyncHealthDataUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PairingViewModel(
    private val requestPairingCode: RequestPairingCodeUseCase,
    private val observePairingEvents: ObservePairingEventsUseCase,
    private val isWatchPaired: IsWatchPairedUseCase,
    private val resetPairing: ResetPairingUseCase,
    private val syncHealthData: SyncHealthDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Loading)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncStatus: StateFlow<SyncUiState> = _syncStatus.asStateFlow()

    private var countdownJob: Job? = null
    private var eventsJob: Job? = null
    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            if (isWatchPaired()) {
                _uiState.value = PairingUiState.Home
            } else {
                loadCode()
            }
        }
    }

    fun goToHome() {
        countdownJob?.cancel()
        eventsJob?.cancel()
        _uiState.value = PairingUiState.Home
    }

    fun rePair() {
        viewModelScope.launch {
            resetPairing()
            loadCode()
        }
    }

    fun syncNow() {
        if (_syncStatus.value is SyncUiState.Syncing) return
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            _syncStatus.value = SyncUiState.Syncing
            _syncStatus.value = try {
                SyncUiState.Done(syncHealthData())
            } catch (e: Exception) {
                SyncUiState.Error
            }
            delay(2_000)
            _syncStatus.value = SyncUiState.Idle
        }
    }

    fun loadCode() {
        countdownJob?.cancel()
        eventsJob?.cancel()
        _uiState.value = PairingUiState.Loading
        viewModelScope.launch {
            try {
                val code = requestPairingCode()
                _uiState.value = PairingUiState.Success(code, code.validForSeconds)
                startCountdown(code)
                observeEvents(code)
            } catch (e: Exception) {
                _uiState.value = PairingUiState.Error(
                    e.message ?: "Impossible de récupérer le code. Réessayez."
                )
            }
        }
    }

    private fun startCountdown(code: PairingCode) {
        countdownJob = viewModelScope.launch {
            var remaining = code.validForSeconds
            while (remaining > 0) {
                delay(1_000)
                remaining--
                (_uiState.value as? PairingUiState.Success)?.let {
                    _uiState.value = it.copy(remainingSeconds = remaining)
                }
            }
            loadCode()
        }
    }

    private fun observeEvents(code: PairingCode) {
        eventsJob = viewModelScope.launch {
            observePairingEvents(code.pairingId)
                .catch { /* flux SSE interrompu : le compte à rebours relancera un code */ }
                .collect { event ->
                    when (event) {
                        is PairingEvent.Confirmed -> {
                            countdownJob?.cancel()
                            _uiState.value = PairingUiState.Confirmed
                        }
                        PairingEvent.Expired -> loadCode()
                        PairingEvent.Pending -> Unit
                    }
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PairingViewModel(
                    ServiceLocator.provideRequestPairingCodeUseCase(),
                    ServiceLocator.provideObservePairingEventsUseCase(),
                    ServiceLocator.provideIsWatchPairedUseCase(),
                    ServiceLocator.provideResetPairingUseCase(),
                    ServiceLocator.provideSyncHealthDataUseCase(),
                )
            }
        }
    }
}
