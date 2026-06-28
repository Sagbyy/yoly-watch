package com.yoly.watch.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yoly.watch.di.ServiceLocator

class HealthSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        ServiceLocator.provideSyncHealthDataUseCase().invoke()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
