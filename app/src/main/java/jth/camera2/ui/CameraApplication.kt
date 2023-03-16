package jth.camera2.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CameraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}