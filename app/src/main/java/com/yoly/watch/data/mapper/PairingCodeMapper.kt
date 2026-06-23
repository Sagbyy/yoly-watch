package com.yoly.watch.data.mapper

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingEventDto
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingEvent

fun PairingCodeDto.toDomain(): PairingCode = PairingCode(
    pairingId = pairingId,
    value = code,
    validForSeconds = expiresInSeconds,
)

fun PairingEventDto.toDomain(): PairingEvent = when (status.uppercase()) {
    "CONFIRMED" -> PairingEvent.Confirmed(deviceToken.orEmpty())
    "EXPIRED" -> PairingEvent.Expired
    else -> PairingEvent.Pending
}
