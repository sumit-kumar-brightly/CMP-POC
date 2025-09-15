package com.brightlysoftware.brightlypoc

import android.app.Application
import com.brightlysoftware.brightlypoc.di.appModule
import com.brightlysoftware.brightlypoc.util.AndroidAppStartTime
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.brightlysoftware.brightlypoc.util.AppStartTimeTracker
import sdk.pendo.io.Pendo

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

        initializePendo()
        startPendoSession()

    }
    private fun initializePendo() {
        val pendoApiKey = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiJmNzM1YjMzNmRkYmM2ODU5MWFjMDkwNmE3ZDg3MGYzYjAwNzQ1Y2NkMWFlZDkxN2I1YTgzYmNjY2NhOGE3YzVmMmM1MDNjZGZiODNlNjQxYjI1MWM0ZjI5MDc3OTJiMDhjNTZlYmRkMmIxMTI0YWZmMmIzN2IwNjI4MTNiMzM5N2JmMTRlODZkYjQzZmE2NWExMmFkMmFkMDZjYWQ1ZmM4Zjc0Mjg3NjBlODZjMGI2NzVlOWIwMTY0NDc5NTM0MmQ4OTQ1MzNjODc5NTUyYTMyNjY3NDQyMzFiYTU5M2RiMDIzMjM4MzVhZGEyMDI5YmVkNWY2ZGUzMjBkNjk3OWI0NDU2NDk2NjM2ZTc5ZWFmM2U1YzkwYjhkNzZkZjQyNDIuMjM0NDYwZjgxZWZmZjhmNDE1ZDU1ZTdmOTAyN2Y0NGYuMGNmM2NkYjM4ZTQ3ZGIyMTliN2UxOWFkM2JkZDEwZmY4NDIxYWY3ZTBjOWVjZDM1MDVkZTliNWU2MmE4YzI5ZSJ9.TZ0zFVTqcpIWCTIYsF_05xJcYCicoRGP4zAKyoINUp2CH8rlUHbxjcTbcOjuCT8-Q1yC1H2SQwp2P9uoZmaTGOy2Owi46SNNjw-U2U6opqQXTtuYJ7sA03fLWX7wDBMDKn1LDX2ECL-5JR_ktW0RMJ_6QPN3RJPjJp6UGWy2JrA"

        Pendo.setup(
            this,
            pendoApiKey,
            null, // PendoOptions (use only if instructed by Pendo support)
            null  // PendoPhasesCallbackInterface (Optional)
        )
    }
    private fun startPendoSession() {
        val visitorId = "bdf640fa-f270-4861-b297-1db8718f2943"
        val accountId = "ACCOUNT-UNIQUE-ID"
        val visitorData = mapOf("product" to "MME")
        val accountData = mapOf("name" to "Brightly Internal")

        Pendo.startSession(
            visitorId,
            accountId,
            visitorData,
            accountData
        )
    }
}