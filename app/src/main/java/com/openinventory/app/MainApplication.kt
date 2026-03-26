package com.openinventory.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.google.firebase.FirebaseApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Força a inicialização se ainda não foi feita
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }
}