package com.yoly.watch.data.identity

import android.content.Context
import android.provider.Settings
import com.yoly.watch.domain.identity.WatchIdentityProvider

class AndroidIdWatchIdentityProvider(
    private val context: Context,
) : WatchIdentityProvider {

    override suspend fun watchId(): String =
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ).orEmpty()
}
