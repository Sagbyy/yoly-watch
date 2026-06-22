package com.yoly.watch.data.remote

import com.yoly.watch.data.remote.dto.CreatePairingCodeRequest
import com.yoly.watch.data.remote.dto.PairingCodeDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PairingService {
    @POST("pairing/codes")
    suspend fun createPairingCode(@Body request: CreatePairingCodeRequest): PairingCodeDto
}
