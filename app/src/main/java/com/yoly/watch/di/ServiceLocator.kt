package com.yoly.watch.di

import com.yoly.watch.data.remote.MockPairingCodeApi
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.data.repository.PairingCodeRepositoryImpl
import com.yoly.watch.domain.repository.PairingCodeRepository
import com.yoly.watch.domain.usecase.RequestPairingCodeUseCase

object ServiceLocator {

    private val pairingCodeApi: PairingCodeApi by lazy { MockPairingCodeApi() }

    private val pairingCodeRepository: PairingCodeRepository by lazy {
        PairingCodeRepositoryImpl(pairingCodeApi)
    }

    fun provideRequestPairingCodeUseCase(): RequestPairingCodeUseCase =
        RequestPairingCodeUseCase(pairingCodeRepository)
}
