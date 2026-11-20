package org.golnaz.project

import android.app.Application
import org.golnaz.project.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AiRagApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AiRagApplication)
            modules(appModule)
        }
    }
}