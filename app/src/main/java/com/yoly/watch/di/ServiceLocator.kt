@file:OptIn(ExperimentalSerializationApi::class)

package com.yoly.watch.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yoly.watch.data.health.HealthServicesDataSource
import com.yoly.watch.data.health.SimulatedHealthDataSource
import com.yoly.watch.data.identity.AndroidIdWatchIdentityProvider
import com.yoly.watch.data.identity.DataStoreDeviceCredentialsStore
import com.yoly.watch.data.remote.HealthApi
import com.yoly.watch.data.remote.HealthService
import com.yoly.watch.data.remote.MockHealthApi
import com.yoly.watch.data.remote.MockPairingCodeApi
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.data.remote.PairingService
import com.yoly.watch.data.remote.RetrofitHealthApi
import com.yoly.watch.data.remote.RetrofitPairingCodeApi
import com.yoly.watch.data.repository.HealthRepositoryImpl
import com.yoly.watch.data.repository.PairingCodeRepositoryImpl
import com.yoly.watch.domain.health.HealthDataSource
import com.yoly.watch.domain.identity.DeviceCredentialsStore
import com.yoly.watch.domain.identity.WatchIdentityProvider
import com.yoly.watch.domain.repository.HealthRepository
import com.yoly.watch.domain.repository.PairingCodeRepository
import com.yoly.watch.domain.usecase.IsWatchPairedUseCase
import com.yoly.watch.domain.usecase.ObservePairingEventsUseCase
import com.yoly.watch.domain.usecase.RequestPairingCodeUseCase
import com.yoly.watch.domain.usecase.ResetPairingUseCase
import com.yoly.watch.domain.usecase.SyncHealthDataUseCase
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "yoly_watch_prefs")

object ServiceLocator {

    // IP LAN de la machine de dev (montre physique sur le même WiFi).
    // Émulateur : utiliser http://10.0.2.2:3000/ ; prod : l'URL HTTPS publique.
    private const val BASE_URL = "http://192.168.1.191:3000/"
    private const val USE_MOCK = false
    private const val USE_SIMULATED_HEALTH = true

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
        AndroidIdWatchIdentityProvider(appContext)
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

    private val healthDataSource: HealthDataSource by lazy {
        if (USE_SIMULATED_HEALTH) {
            SimulatedHealthDataSource()
        } else {
            HealthServicesDataSource(appContext).also { it.start() }
        }
    }

    private val healthApi: HealthApi by lazy {
        if (USE_MOCK) {
            MockHealthApi()
        } else {
            RetrofitHealthApi(retrofit.create(HealthService::class.java))
        }
    }

    private val healthRepository: HealthRepository by lazy {
        HealthRepositoryImpl(healthDataSource, healthApi, credentialsStore, watchIdentityProvider)
    }

    fun provideSyncHealthDataUseCase(): SyncHealthDataUseCase =
        SyncHealthDataUseCase(healthRepository)

    fun provideRequestPairingCodeUseCase(): RequestPairingCodeUseCase =
        RequestPairingCodeUseCase(pairingCodeRepository)

    fun provideObservePairingEventsUseCase(): ObservePairingEventsUseCase =
        ObservePairingEventsUseCase(pairingCodeRepository)

    fun provideIsWatchPairedUseCase(): IsWatchPairedUseCase =
        IsWatchPairedUseCase(credentialsStore)

    fun provideResetPairingUseCase(): ResetPairingUseCase =
        ResetPairingUseCase(credentialsStore)
}
