package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.model.PairingStatus
import com.yoly.watch.domain.repository.PairingCodeRepository
import kotlinx.coroutines.flow.Flow

class ObservePairingStatusUseCase(
    private val repository: PairingCodeRepository,
) {
    operator fun invoke(pairingId: String): Flow<PairingStatus> =
        repository.observePairingStatus(pairingId)
}
