package com.yoly.watch.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yoly.watch.di.ServiceLocator
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingStatus
import com.yoly.watch.domain.usecase.ObservePairingStatusUseCase
import com.yoly.watch.domain.usecase.RequestPairingCodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PairingViewModel(
    private val requestPairingCode: RequestPairingCodeUseCase,
    private val observePairingStatus: ObservePairingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Loading)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var statusJob: Job? = null

    init {
        loadCode()
    }

    fun loadCode() {
        countdownJob?.cancel()
        statusJob?.cancel()
        _uiState.value = PairingUiState.Loading
        viewModelScope.launch {
            try {
                val code = requestPairingCode()
                _uiState.value = PairingUiState.Success(code, code.validForSeconds)
                startCountdown(code)
                observeStatus(code)
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

    private fun observeStatus(code: PairingCode) {
        statusJob = viewModelScope.launch {
            observePairingStatus(code.pairingId)
                .catch { /* flux SSE interrompu : le compte à rebours relancera un code */ }
                .collect { status ->
                    when (status) {
                        PairingStatus.CONFIRMED -> {
                            countdownJob?.cancel()
                            _uiState.value = PairingUiState.Confirmed
                        }
                        PairingStatus.EXPIRED -> loadCode()
                        PairingStatus.PENDING -> Unit
                    }
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                PairingViewModel(
                    ServiceLocator.provideRequestPairingCodeUseCase(),
                    ServiceLocator.provideObservePairingStatusUseCase(),
                )
            }
        }
    }
}
