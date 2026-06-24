package com.yoly.watch.domain.identity

interface DeviceCredentialsStore {
    suspend fun saveDeviceToken(token: String)
    suspend fun deviceToken(): String?
    suspend fun clearDeviceToken()
}
