package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.repository.PairingCodeRepository

class RequestPairingCodeUseCase(
    private val repository: PairingCodeRepository,
) {
    suspend operator fun invoke(): PairingCode = repository.requestPairingCode()
}
