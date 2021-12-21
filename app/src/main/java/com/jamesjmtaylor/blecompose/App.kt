package com.jamesjmtaylor.blecompose

import android.app.Application
import android.graphics.Region
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import timber.log.Timber

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Timber.d("App started up")
    }
}