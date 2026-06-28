package com.yoly.watch.data.health

import android.content.Context
import android.os.SystemClock
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import com.yoly.watch.domain.health.HealthDataSource
import com.yoly.watch.domain.model.HealthSample
import com.yoly.watch.domain.model.HeartRateSample
import com.yoly.watch.domain.model.StepCountSample
import java.time.Instant
import java.util.Collections

class HealthServicesDataSource(context: Context) : HealthDataSource {

    private val passiveClient = HealthServices.getClient(context).passiveMonitoringClient

    private val buffer = Collections.synchronizedList(mutableListOf<HealthSample>())

    private val config = PassiveListenerConfig.builder()
        .setDataTypes(setOf(DataType.HEART_RATE_BPM, DataType.STEPS_DAILY))
        .build()

    private val callback = object : PassiveListenerCallback {
        override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
            val bootInstant = Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime())
            dataPoints.getData(DataType.HEART_RATE_BPM).forEach { point ->
                buffer += HeartRateSample(
                    beatsPerMinute = point.value.toInt(),
                    recordedAt = point.getTimeInstant(bootInstant),
                )
            }
            dataPoints.getData(DataType.STEPS_DAILY).forEach { point ->
                buffer += StepCountSample(
                    count = point.value.toInt(),
                    recordedAt = point.getEndInstant(bootInstant),
                )
            }
        }
    }

    fun start() {
        passiveClient.setPassiveListenerCallback(config, callback)
    }

    override suspend fun collect(): List<HealthSample> = synchronized(buffer) {
        buffer.toList().also { buffer.clear() }
    }
}
