@file:OptIn(ExperimentalSerializationApi::class)

package com.yoly.watch.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yoly.watch.data.identity.DataStoreDeviceCredentialsStore
import com.yoly.watch.data.identity.DataStoreWatchIdentityProvider
import com.yoly.watch.data.remote.MockPairingCodeApi
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.data.remote.PairingService
import com.yoly.watch.data.remote.RetrofitPairingCodeApi
import com.yoly.watch.data.repository.PairingCodeRepositoryImpl
import com.yoly.watch.domain.identity.DeviceCredentialsStore
import com.yoly.watch.domain.identity.WatchIdentityProvider
import com.yoly.watch.domain.repository.PairingCodeRepository
import com.yoly.watch.domain.usecase.ObservePairingEventsUseCase
import com.yoly.watch.domain.usecase.RequestPairingCodeUseCase
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "yoly_watch_prefs")

object ServiceLocator {

    // 10.0.2.2 = host "localhost" vu depuis l'émulateur Android. Sur un appareil
    // réel, remplacer par l'IP LAN de la machine de dev (ou l'URL de prod).
    private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val USE_MOCK = false

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val watchIdentityProvider: WatchIdentityProvider by lazy {
        DataStoreWatchIdentityProvider(appContext.dataStore)
    }

    private val credentialsStore: DeviceCredentialsStore by lazy {
        DataStoreDeviceCredentialsStore(appContext.dataStore)
    }

    private val pairingCodeApi: PairingCodeApi by lazy {
        if (USE_MOCK) {
            MockPairingCodeApi()
        } else {
            RetrofitPairingCodeApi(
                service = retrofit.create(PairingService::class.java),
                client = okHttpClient,
                baseUrl = BASE_URL,
                json = json,
            )
        }
    }

    private val pairingCodeRepository: PairingCodeRepository by lazy {
        PairingCodeRepositoryImpl(pairingCodeApi, watchIdentityProvider, credentialsStore)
    }

    fun provideRequestPairingCodeUseCase(): RequestPairingCodeUseCase =
        RequestPairingCodeUseCase(pairingCodeRepository)

    fun provideObservePairingEventsUseCase(): ObservePairingEventsUseCase =
        ObservePairingEventsUseCase(pairingCodeRepository)
}
