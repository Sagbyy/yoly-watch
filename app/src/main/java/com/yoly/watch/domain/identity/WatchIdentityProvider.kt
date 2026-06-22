package com.yoly.watch.domain.identity

interface WatchIdentityProvider {
    suspend fun watchId(): String
}
