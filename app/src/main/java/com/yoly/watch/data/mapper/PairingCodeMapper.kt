package com.yoly.watch.data.mapper

import com.yoly.watch.data.remote.dto.PairingCodeDto
import com.yoly.watch.domain.model.PairingCode

fun PairingCodeDto.toDomain(): PairingCode = PairingCode(
    value = code,
    validForSeconds = expiresInSeconds,
)
