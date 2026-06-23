package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.model.PairingEvent
import com.yoly.watch.domain.repository.PairingCodeRepository
import kotlinx.coroutines.flow.Flow

class ObservePairingEventsUseCase(
    private val repository: PairingCodeRepository,
) {
    operator fun invoke(pairingId: String): Flow<PairingEvent> =
        repository.observePairingEvents(pairingId)
}
