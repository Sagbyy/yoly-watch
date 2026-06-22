package com.yoly.watch

import android.app.Application
import com.yoly.watch.di.ServiceLocator

class YolyWatchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
