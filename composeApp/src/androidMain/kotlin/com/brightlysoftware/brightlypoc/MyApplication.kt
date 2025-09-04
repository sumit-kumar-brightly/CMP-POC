package com.brightlysoftware.brightlypoc

import android.app.Application
import com.brightlysoftware.brightlypoc.di.appModule
import com.brightlysoftware.brightlypoc.util.AndroidAppStartTime
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.brightlysoftware.brightlypoc.util.AppStartTimeTracker

class MyApplication : Application() {
    init {
        // Force the static initializer to run early
        val startTime = AndroidAppStartTime.startTime
        AppStartTimeTracker.recordAppStart(startTime)
    }
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }

    }
}