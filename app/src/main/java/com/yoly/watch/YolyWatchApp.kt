package com.yoly.watch

import android.app.Application
import com.yoly.watch.di.ServiceLocator
import com.yoly.watch.worker.HealthSyncScheduler

class YolyWatchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        HealthSyncScheduler.schedule(this)
    }
}
