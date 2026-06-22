@file:OptIn(ExperimentalSerializationApi::class)

package com.yoly.watch.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yoly.watch.data.identity.DataStoreWatchIdentityProvider
import com.yoly.watch.data.remote.MockPairingCodeApi
import com.yoly.watch.data.remote.PairingCodeApi
import com.yoly.watch.data.remote.PairingService
import com.yoly.watch.data.remote.RetrofitPairingCodeApi
import com.yoly.watch.data.repository.PairingCodeRepositoryImpl
import com.yoly.watch.domain.identity.WatchIdentityProvider
import com.yoly.watch.domain.repository.PairingCodeRepository
import com.yoly.watch.domain.usecase.ObservePairingStatusUseCase
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

    // TODO: replace with the real Yoly API base URL, then set USE_MOCK = false.
    private const val BASE_URL = "https://api.yoly.app/"
    private const val USE_MOCK = true

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
        PairingCodeRepositoryImpl(pairingCodeApi, watchIdentityProvider)
    }

    fun provideRequestPairingCodeUseCase(): RequestPairingCodeUseCase =
        RequestPairingCodeUseCase(pairingCodeRepository)

    fun provideObservePairingStatusUseCase(): ObservePairingStatusUseCase =
        ObservePairingStatusUseCase(pairingCodeRepository)
}
