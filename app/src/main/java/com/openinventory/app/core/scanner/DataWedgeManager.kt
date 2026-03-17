package com.openinventory.app.core.scanner

import android.content.Context
import android.content.Intent
import android.os.Bundle

class DataWedgeManager(private val context: Context) {

    private val PROFILE_NAME = "OpenInventoryProfile"

    fun createProfile() {
        sendCommand("com.symbol.datawedge.api.CREATE_PROFILE", PROFILE_NAME)
        setupProfile()
    }

    private fun setupProfile() {

        val profileConfig = Bundle().apply {
            putString("PROFILE_NAME", PROFILE_NAME)
            putString("PROFILE_ENABLED", "true")
            putString("CONFIG_MODE", "UPDATE")

            // Associa ao app
            putParcelableArray(
                "APP_LIST",
                arrayOf(
                    Bundle().apply {
                        putString("PACKAGE_NAME", "com.openinventory.app")
                        putStringArray("ACTIVITY_LIST", arrayOf("*"))
                    }
                )
            )

            // Configuração do scanner
            putBundle("PLUGIN_CONFIG", Bundle().apply {
                putString("PLUGIN_NAME", "BARCODE")
                putString("RESET_CONFIG", "true")
                putBundle("PARAM_LIST", Bundle())
            })

            // Configuração do Intent Output (ESSENCIAL)
            putBundle("PLUGIN_CONFIG", Bundle().apply {
                putString("PLUGIN_NAME", "INTENT")
                putString("RESET_CONFIG", "true")
                putBundle("PARAM_LIST", Bundle().apply {
                    putString("intent_output_enabled", "true")
                    putString("intent_action", "com.openinventory.app.SCAN")
                    putString("intent_delivery", "2") // 2 = Broadcast
                })
            })
        }

        val intent = Intent("com.symbol.datawedge.api.SET_CONFIG")
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", profileConfig)
        context.sendBroadcast(intent)
    }

    private fun sendCommand(command: String, parameter: String) {
        val intent = Intent()
        intent.action = "com.symbol.datawedge.api.ACTION"
        intent.putExtra(command, parameter)
        context.sendBroadcast(intent)
    }
}