package com.openinventory.app.core.config

object TokenManager {
    var token: String? = null

    fun getBearerToken(): String? {
        val t = token?.trim() ?: return null
        // O seu Java espera "Bearer " (com o espaço).
        // Vamos garantir que a String seja montada exatamente assim:
        return "Bearer $t"
    }
}