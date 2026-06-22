package com.yoly.watch.data.identity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yoly.watch.domain.identity.WatchIdentityProvider
import java.util.UUID

class DataStoreWatchIdentityProvider(
    private val dataStore: DataStore<Preferences>,
) : WatchIdentityProvider {

    override suspend fun watchId(): String {
        val prefs = dataStore.edit { prefs ->
            if (prefs[KEY] == null) {
                prefs[KEY] = UUID.randomUUID().toString()
            }
        }
        return prefs[KEY]!!
    }

    private companion object {
        val KEY = stringPreferencesKey("watch_id")
    }
}
