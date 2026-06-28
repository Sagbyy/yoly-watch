package com.yoly.watch.domain.repository

interface HealthRepository {
    suspend fun syncNow(): Int
}
