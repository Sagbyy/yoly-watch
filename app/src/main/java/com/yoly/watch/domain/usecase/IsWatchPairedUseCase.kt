package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.identity.DeviceCredentialsStore

class IsWatchPairedUseCase(
    private val credentialsStore: DeviceCredentialsStore,
) {
    suspend operator fun invoke(): Boolean = credentialsStore.deviceToken() != null
}
