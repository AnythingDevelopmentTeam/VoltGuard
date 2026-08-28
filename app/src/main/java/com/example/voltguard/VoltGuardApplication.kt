package com.example.voltguard

import android.app.Application
import android.content.Context
import com.example.voltguard.data.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class VoltGuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VoltGuardApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { SettingsManager(androidContext()) }
    single { SessionTracker(androidContext()) }
    single { BatteryHistoryManager(androidContext()) }
}
