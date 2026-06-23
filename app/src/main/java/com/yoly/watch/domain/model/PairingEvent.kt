package com.yoly.watch.domain.model

sealed interface PairingEvent {
    data object Pending : PairingEvent
    data class Confirmed(val deviceToken: String) : PairingEvent
    data object Expired : PairingEvent
}
