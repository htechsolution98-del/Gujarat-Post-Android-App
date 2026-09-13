package com.gujaratpost.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class GujaratPostApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Set default night mode to follow system
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    companion object {
        lateinit var instance: GujaratPostApp
            private set
    }
}
