package com.yoly.watch.data.repository

import com.yoly.watch.data.mapper.toDomain
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.repository.PairingCodeRepository

class PairingCodeRepositoryImpl(
    private val api: PairingCodeApi,
) : PairingCodeRepository {

    override suspend fun requestPairingCode(): PairingCode =
        api.fetchPairingCode().toDomain()
}
