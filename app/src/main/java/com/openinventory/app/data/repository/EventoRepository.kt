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

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

            // AQUI: Deve ser "imagem", conforme o erro do log indicou!
            val body = MultipartBody.Part.createFormData("imagem", file.name, requestFile)

            val nomePart = nomeLimpo.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadImagem(token = token, imagem = body, nome = nomePart)

            if (response.isSuccessful) {
                val nomeSalvo = response.body()
                Log.d("REPO_UPLOAD", "Sucesso! Nome retornado: $nomeSalvo")
                nomeSalvo // Retorna o nome do arquivo (ex: leaguecup.jpg)
            } else {
                // O erro 400 caiu aqui antes porque o nome estava 'file'
                Log.e("REPO_UPLOAD", "Erro no servidor: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("REPO_UPLOAD", "Exceção: ${e.message}")
            null
        }
    }
    suspend fun excluirEvento(id: Long): Boolean {
        return try {
            val token = TokenManager.getBearerToken() ?: ""
            val response = apiService.excluirEvento(token, id)

            // No Postman deu 204, então checamos isSuccessful ou o código 204
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("REPO_EVENTO", "Erro ao excluir: ${e.message}")
            false
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