package com.example.showtime

import android.app.Application
import com.example.showtime.di.initKoinIfNeeded
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinIfNeeded {
            androidContext(this@MyApplication)
        }
    }
}
