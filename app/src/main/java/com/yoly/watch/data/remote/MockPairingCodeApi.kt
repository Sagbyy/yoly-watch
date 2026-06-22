package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingStatusDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class MockPairingCodeApi(
    private val responseDelayMillis: Long = 800L,
    private val confirmAfterMillis: Long = 8_000L,
) : PairingCodeApi {

    override suspend fun fetchPairingCode(watchId: String): PairingCodeDto {
        delay(responseDelayMillis)
        val code = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
        return PairingCodeDto(
            pairingId = "mock-$watchId-$code",
            code = code,
            expiresInSeconds = VALIDITY_SECONDS,
        )
    }

    override fun observeStatus(pairingId: String): Flow<PairingStatusDto> = flow {
        emit(PairingStatusDto("PENDING"))
        delay(confirmAfterMillis)
        emit(PairingStatusDto("CONFIRMED"))
    }

    private companion object {
        const val VALIDITY_SECONDS = 120L
    }
}
