package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.identity.DeviceCredentialsStore

class ResetPairingUseCase(
    private val credentialsStore: DeviceCredentialsStore,
) {
    suspend operator fun invoke() = credentialsStore.clearDeviceToken()
}
