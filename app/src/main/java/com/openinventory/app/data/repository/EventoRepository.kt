package com.openinventory.app.data.repository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.openinventory.app.core.config.TokenManager
import com.openinventory.app.service.RayearthApiService
import android.util.Log
import com.openinventory.app.service.JogoResponseDTO
import com.openinventory.app.service.EventoRequestDTO
import com.openinventory.app.service.EventoDTO
import com.openinventory.app.service.*
class EventoRepository(private val apiService: RayearthApiService) {

    suspend fun uploadImagem(file: File, nomeLimpo: String): String? {
        return try {
            val token = TokenManager.getBearerToken() ?: ""

            // Prepara o arquivo para o Multipart
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("imagem", file.name, requestFile)

            // Prepara o nome (limpo) como RequestBody
            val nomePart = nomeLimpo.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadImagem(token = token, imagem = body, nome = nomePart)

            if (response.isSuccessful) {
                // Retorna o corpo da resposta (geralmente o nome do arquivo salvo)
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun buscarJogos(): List<JogoResponseDTO> {
        return try {
            apiService.listarJogos()
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun buscarEventos(): List<EventoDTO> {
        return try {
            apiService.listarEventos()
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun buscarParticipantes(eventoId: Long): List<String> { // Alterado para String
        return try {
            apiService.listarParticipantes(eventoId)
        } catch (e: Exception) {
            Log.e("REPO", "Erro ao buscar participantes: ${e.message}")
            emptyList()
        }
    }

    suspend fun cadastrarEvento(evento: EventoRequestDTO): Boolean {
        return try {
            val token = TokenManager.getBearerToken() ?: ""
            val response = apiService.criarEvento(token, evento)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}