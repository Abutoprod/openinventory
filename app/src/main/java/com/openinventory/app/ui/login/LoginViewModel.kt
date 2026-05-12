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

    // No LoginViewModel.kt

    fun solicitarCodigoSenha(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.solicitarCodigoSenha(mapOf("email" to email))
                if (response.isSuccessful) {
                    onSuccess() // Agora vai chegar aqui!
                } else {
                    onError("Erro no servidor")
                }
            } catch (e: Exception) {
                // O erro MalformedJsonException parará de cair aqui se usar Response<Unit>
                onError("Erro de conexão")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun redefinirSenha(
        context: android.content.Context, // Precisamos do context para vibrar
        token: String,
        novaSenha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.redefinirSenha(mapOf("token" to token, "novaSenha" to novaSenha))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    vibrarErro(context)
                    onError("Código inválido!")
                }
            } catch (e: Exception) {
                vibrarErro(context)
                onError("Erro de conexão")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun vibrarErro(context: android.content.Context) {
        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }
}