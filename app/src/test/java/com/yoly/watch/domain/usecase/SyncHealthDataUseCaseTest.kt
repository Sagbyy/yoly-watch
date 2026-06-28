package com.yoly.watch.domain.usecase

import com.yoly.watch.domain.repository.HealthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncHealthDataUseCaseTest {

    @Test
    fun `delegates to the repository and returns the uploaded count`() = runTest {
        val repository = object : HealthRepository {
            var called = false
            override suspend fun syncNow(): Int {
                called = true
                return 4
            }
        }

        val result = SyncHealthDataUseCase(repository).invoke()

        assertEquals(4, result)
        assertEquals(true, repository.called)
    }
}
