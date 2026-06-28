package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.repository.HealthRepository

class SyncHealthDataUseCase(
    private val repository: HealthRepository,
) {
    suspend operator fun invoke(): Int = repository.syncNow()
}
