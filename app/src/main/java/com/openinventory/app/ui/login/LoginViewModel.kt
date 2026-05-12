package com.openinventory.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.core.config.RetrofitClient
import com.openinventory.app.service.LoginRequest
import com.openinventory.app.service.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    fun realizarLogin(email: String, senha: String, onLoginSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            try {
                // Usa o LoginRequest do pacote .service
                val response = RetrofitClient.instance.login(LoginRequest(email, senha))

                if (response.isSuccessful) {
                    val body = response.body()
                    // Mudança aqui: Se houver um token, nós deixamos entrar.
                    if (body?.token != null) {
                        android.util.Log.d("DEBUG_LOGIN", "Token recebido com sucesso: ${body.token}")
                        onLoginSuccess(body.token)
                    } else {
                        android.util.Log.e("DEBUG_LOGIN", "Corpo da resposta vazio ou sem token!")
                        _loginError.value = "Erro: Servidor não enviou o token."
                    }
                } else {
                    _loginError.value = "E-mail ou senha incorretos."
                }
            } catch (e: Exception) {
                _loginError.value = "Erro de conexão: Verifique o ngrok."
            } finally {
                _isLoading.value = false
            }
        }
    }
}