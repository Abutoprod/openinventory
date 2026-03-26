package com.openinventory.app

import android.app.Application
import com.google.firebase.FirebaseApp

class OpenInventoryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}