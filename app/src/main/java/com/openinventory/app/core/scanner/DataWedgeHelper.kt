package com.openinventory.app.core.scanner

import android.content.Context
import android.content.Intent
import android.os.Bundle

object DataWedgeHelper {

    fun createProfile(context: Context) {

        val profileName = "OpenInventory"

        val intent = Intent()
        intent.action = "com.symbol.datawedge.api.ACTION"

        // Criar profile
        val createProfileBundle = Bundle()
        createProfileBundle.putString("PROFILE_NAME", profileName)
        createProfileBundle.putString("PROFILE_ENABLED", "true")
        createProfileBundle.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST")

        // App association
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", context.packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))

        // Configuração do Intent Output
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")

        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", "com.scanner.broadcast")
        intentProps.putString("intent_delivery", "2") // 2 = broadcast

        intentConfig.putBundle("PARAM_LIST", intentProps)

        createProfileBundle.putParcelableArray("APP_LIST", arrayOf(appConfig))
        createProfileBundle.putParcelableArray("PLUGIN_CONFIG", arrayOf(intentConfig))

        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", createProfileBundle)

        context.sendBroadcast(intent)
    }
}