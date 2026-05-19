package com.example.simpleliveroom

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class LiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}