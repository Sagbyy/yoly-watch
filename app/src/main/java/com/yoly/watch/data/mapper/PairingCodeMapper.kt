package com.yoly.watch.data.mapper

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.data.remote.dto.PairingStatusDto
import com.yoly.watch.domain.model.PairingCode
import com.yoly.watch.domain.model.PairingStatus

fun PairingCodeDto.toDomain(): PairingCode = PairingCode(
    pairingId = pairingId,
    value = code,
    validForSeconds = expiresInSeconds,
)

fun PairingStatusDto.toDomain(): PairingStatus = when (status.uppercase()) {
    "CONFIRMED" -> PairingStatus.CONFIRMED
    "EXPIRED" -> PairingStatus.EXPIRED
    else -> PairingStatus.PENDING
}
