package com.yoly.watch.data.identity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yoly.watch.domain.identity.DeviceCredentialsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreDeviceCredentialsStore(
    private val dataStore: DataStore<Preferences>,
) : DeviceCredentialsStore {

    override suspend fun saveDeviceToken(token: String) {
        dataStore.edit { it[KEY] = token }
    }

    override suspend fun deviceToken(): String? =
        dataStore.data.map { it[KEY] }.first()

    private companion object {
        val KEY = stringPreferencesKey("device_token")
    }
}
