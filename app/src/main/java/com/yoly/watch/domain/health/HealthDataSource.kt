package com.yoly.watch.domain.health

import com.yoly.watch.domain.model.HealthSample

interface HealthDataSource {
    suspend fun collect(): List<HealthSample>
}
