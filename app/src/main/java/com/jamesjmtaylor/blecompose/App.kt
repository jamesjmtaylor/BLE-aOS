package com.jamesjmtaylor.blecompose

import android.app.Application
import android.graphics.Region
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import timber.log.Timber

class App : Application(), ViewModelStoreOwner {

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

    private val appViewModelStore: ViewModelStore by lazy { ViewModelStore() }
    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
    }
}