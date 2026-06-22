package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.PairingCodeDto
import kotlinx.coroutines.delay
import kotlin.random.Random

class MockPairingCodeApi(
    private val responseDelayMillis: Long = 800L,
) : PairingCodeApi {

    override suspend fun fetchPairingCode(): PairingCodeDto {
        delay(responseDelayMillis)
        val code = Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
        return PairingCodeDto(code = code, expiresInSeconds = VALIDITY_SECONDS)
    }

    private companion object {
        const val VALIDITY_SECONDS = 120L
    }
}
