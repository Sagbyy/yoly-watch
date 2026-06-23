package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingEventDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class MockPairingCodeApi(
    private val responseDelayMillis: Long = 800L,
    private val confirmAfterMillis: Long = 8_000L,
) : PairingCodeApi {

    override suspend fun fetchPairingCode(deviceUuid: String): PairingCodeDto {
        delay(responseDelayMillis)
        val code = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
        return PairingCodeDto(
            pairingId = "mock-$deviceUuid-$code",
            code = code,
            expiresInSeconds = VALIDITY_SECONDS,
        )
    }

    override fun observeEvents(pairingId: String): Flow<PairingEventDto> = flow {
        emit(PairingEventDto(status = "PENDING"))
        delay(confirmAfterMillis)
        emit(PairingEventDto(status = "CONFIRMED", deviceToken = "dvc_mock_token"))
    }

    private companion object {
        const val VALIDITY_SECONDS = 120L
    }
}
