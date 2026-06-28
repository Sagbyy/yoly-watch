package com.yoly.watch.presentation.pairing

import com.yoly.watch.domain.model.PairingCode

sealed interface PairingUiState {
    data object Loading : PairingUiState
    data class Success(
        val code: PairingCode,
        val remainingSeconds: Long,
    ) : PairingUiState {
        val progress: Float
            get() = (remainingSeconds.toFloat() / code.validForSeconds).coerceIn(0f, 1f)
    }
    data object Confirmed : PairingUiState
    data object Home : PairingUiState
    data class Error(val message: String) : PairingUiState
}

sealed interface SyncUiState {
    data object Idle : SyncUiState
    data object Syncing : SyncUiState
    data class Done(val count: Int) : SyncUiState
    data object Error : SyncUiState
}
