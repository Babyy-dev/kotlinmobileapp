package com.kappa.app

import android.app.Application
import com.kappa.app.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class KappaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
            Timber.d("Application created")
        } catch (e: Exception) {
            // Silent fail for Timber initialization - app should still work
            android.util.Log.e("KappaApplication", "Failed to initialize Timber", e)
        }
    }
}
