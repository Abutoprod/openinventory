package com.openinventory.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.openinventory.app.R
import androidx.compose.foundation.shape.CircleShape

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToMenu: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showEsqueciSenhaDialog by remember { mutableStateOf(false) }
    var showRedefinirSenhaDialog by remember { mutableStateOf(false) }
    var emailRecuperacao by remember { mutableStateOf("") }
    var codigoRecuperacao by remember { mutableStateOf("") }
    var novaSenhaRecuperacao by remember { mutableStateOf("") }


    Box(modifier = Modifier.fillMaxSize()) {
        // Fundo com Degradê igual ao Menu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.orange_back).copy(alpha = 0.9f),
                            colorResource(R.color.yellow_back).copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Circular Estilizada
            Surface(
                modifier = Modifier.size(120.dp), // Aumentado um pouco para destaque
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f), // Transparência elegante
                border = BorderStroke(2.dp, Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rayeart),
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "ADMIN LOGIN",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp // Mais espaçamento para ar de "Admin"
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Inputs Estilizados com Glassmorphism (leve fundo branco transparente)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    // Adiciona um leve fundo para facilitar a leitura sobre o degradê
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                )
            )
            TextButton(
                onClick = { showEsqueciSenhaDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Esqueci minha senha", color = Color.White.copy(alpha = 0.8f))
            }

            // Mensagem de Erro mais visível
            if (loginError != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        loginError!!,
                        color = Color.Yellow,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Diálogo para pedir o E-mail
            if (showEsqueciSenhaDialog) {
                AlertDialog(
                    onDismissRequest = { showEsqueciSenhaDialog = false },
                    title = { Text("Recuperar Senha") },
                    text = {
                        Column {
                            Text("Digite seu e-mail para receber o código de 6 dígitos.")
                            OutlinedTextField(
                                value = emailRecuperacao,
                                onValueChange = { emailRecuperacao = it },
                                label = { Text("E-mail") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.solicitarCodigoSenha(emailRecuperacao,
                                onSuccess = {
                                    showEsqueciSenhaDialog = false
                                    showRedefinirSenhaDialog = true // Pula para o próximo passo
                                },
                                onError = { /* Mostrar erro se quiser */ }
                            )
                        }) { Text("ENVIAR CÓDIGO") }
                    }
                )
            }
            val context = LocalContext.current

// 2. Diálogo para digitar o Código e a Nova Senha
            if (showRedefinirSenhaDialog) {
                AlertDialog(
                    onDismissRequest = { showRedefinirSenhaDialog = false },
                    title = { Text("Nova Senha") },
                    text = {
                        Column {
                            Text("Digite o código recebido e sua nova senha.")
                            OutlinedTextField(
                                value = codigoRecuperacao,
                                onValueChange = { codigoRecuperacao = it },
                                label = { Text("Código de 6 dígitos") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = novaSenhaRecuperacao,
                                onValueChange = { novaSenhaRecuperacao = it },
                                label = { Text("Nova Senha") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.redefinirSenha(
                                context = context,
                                token = codigoRecuperacao,
                                novaSenha = novaSenhaRecuperacao,
                                onSuccess = {
                                    // FECHA TUDO E LIMPA CAMPOS
                                    showRedefinirSenhaDialog = false
                                    codigoRecuperacao = ""
                                    novaSenhaRecuperacao = ""
                                    // Aqui você pode disparar um Toast de "Sucesso!"
                                    android.widget.Toast.makeText(context, "Senha alterada!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = { msg ->
                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }) {
                            Text("ALTERAR SENHA")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.realizarLogin(email, senha) { tokenRecebido ->
                        com.openinventory.app.core.config.TokenManager.token = tokenRecebido
                        onNavigateToMenu()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorResource(R.color.orange_back),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "ENTRAR",
                        color = colorResource(R.color.orange_back),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}