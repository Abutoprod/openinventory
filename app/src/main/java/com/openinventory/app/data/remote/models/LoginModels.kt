package com.openinventory.app.data.remote.models

data class LoginRequest(
    val email: String,
    val senha: String // Deve bater com o nome do campo no seu DTO Java
)

data class LoginResponse(
    val token: String,
    val role: String // Usaremos isso para garantir que é ADMIN
)