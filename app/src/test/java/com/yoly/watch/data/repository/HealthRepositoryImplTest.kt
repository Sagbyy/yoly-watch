package com.yoly.watch.data.repository

import com.yoly.watch.domain.model.HeartRateSample
import com.yoly.watch.domain.model.StepCountSample
import com.yoly.watch.testutil.FakeDeviceCredentialsStore
import com.yoly.watch.testutil.FakeHealthApi
import com.yoly.watch.testutil.FakeHealthDataSource
import com.yoly.watch.testutil.FakeWatchIdentityProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class HealthRepositoryImplTest {

    private val now: Instant = Instant.parse("2026-06-27T10:00:00Z")

    @Test
    fun `uploads collected samples with the bearer token and watch id`() = runTest {
        val api = FakeHealthApi()
        val repo = HealthRepositoryImpl(
            dataSource = FakeHealthDataSource(
                samples = listOf(
                    HeartRateSample(70, now),
                    StepCountSample(12, now),
                ),
            ),
            api = api,
            credentialsStore = FakeDeviceCredentialsStore(token = "dvc_token"),
            watchIdentityProvider = FakeWatchIdentityProvider("watch-99"),
        )

        val sent = repo.syncNow()

        assertEquals(2, sent)
        assertEquals("dvc_token", api.lastToken)
        assertEquals(1, api.uploaded.size)
        assertEquals("watch-99", api.uploaded.first().watchId)
        assertEquals(listOf("HEART_RATE", "STEPS"), api.uploaded.first().samples.map { it.type })
    }

    @Test
    fun `does not upload when the watch is not paired`() = runTest {
        val api = FakeHealthApi()
        val source = FakeHealthDataSource(samples = listOf(HeartRateSample(70, now)))
        val repo = HealthRepositoryImpl(
            dataSource = source,
            api = api,
            credentialsStore = FakeDeviceCredentialsStore(token = null),
            watchIdentityProvider = FakeWatchIdentityProvider(),
        )

        val sent = repo.syncNow()

        assertEquals(0, sent)
        assertTrue(api.uploaded.isEmpty())
        assertEquals(0, source.collectCount)
    }

    @Test
    fun `does not upload an empty batch`() = runTest {
        val api = FakeHealthApi()
        val repo = HealthRepositoryImpl(
            dataSource = FakeHealthDataSource(samples = emptyList()),
            api = api,
            credentialsStore = FakeDeviceCredentialsStore(token = "dvc_token"),
            watchIdentityProvider = FakeWatchIdentityProvider(),
        )

        val sent = repo.syncNow()

        assertEquals(0, sent)
        assertTrue(api.uploaded.isEmpty())
    }
}
