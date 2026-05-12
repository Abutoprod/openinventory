package com.openinventory.app.ui.eventos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.data.repository.EventoRepository
import com.openinventory.app.service.EventoDTO
import com.openinventory.app.service.EventoRequestDTO
import com.openinventory.app.service.JogoResponseDTO
import kotlinx.coroutines.launch
import com.openinventory.app.core.utils.FileUtil
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.openinventory.app.service.ParticipanteDTO
import java.io.File

class EventoViewModel(private val repository: EventoRepository) : ViewModel() {

    var jogos = mutableStateListOf<JogoResponseDTO>()
    var isLoading = mutableStateOf(false)
    var mensagemErro = mutableStateOf<String?>(null)
    var listaEventos = mutableStateListOf<EventoDTO>()

    var participantes = mutableStateListOf<String>()
        private set // Apenas o ViewModel pode alterar a lista

    var carregandoParticipantes = mutableStateOf(false)
        private set


    fun carregarJogos() {
        viewModelScope.launch {
            val lista = repository.buscarJogos()
            jogos.clear()
            jogos.addAll(lista)
        }
    }

    fun carregarEventos() {
        viewModelScope.launch {
            val lista = repository.buscarEventos()
            listaEventos.clear()
            listaEventos.addAll(lista)
        }
    }

    // Função auxiliar para converter URI em Arquivo Real
    private fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return file
    }

    fun cadastrarEventoComImagem(
        context: Context,
        uri: Uri?,
        titulo: String,
        descricao: String,
        dataIso: String,
        filialId: Long,
        jogoId: Long,
        jogoNome: String,
        isSemanal: Boolean,
        nomeImagemPredefinido: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true

            // 1. Gerar o nome que sugerimos ao servidor (limpo)
            val nomeSugerido = if (isSemanal) "semanal_${jogoNome.lowercase()}" else titulo.lowercase()
            val nomeLimpo = nomeSugerido.replace(" ", "_").replace(":", "").replace("/", "")

            var nomeImagemParaOBanco = ""

            // 2. Tentar o upload primeiro
            if (uri != null) {
                val file = FileUtil.getFileFromUri(context, uri)
                // IMPORTANTE: O repository deve retornar o nome do ficheiro salvo
                val resultadoUpload = repository.uploadImagem(file, nomeLimpo)

                if (!resultadoUpload.isNullOrBlank()) {
                    // Se o servidor devolveu "foto.jpg", guardamos isso
                    nomeImagemParaOBanco = resultadoUpload
                } else {
                    Log.e("EVENTO", "Upload falhou, o evento será criado sem imagem")
                }
            }

            // 3. Criar o evento com o nome da imagem que veio do servidor
            val novoEvento = EventoRequestDTO(
                titulo = titulo,
                descricao = descricao,
                dataHora = dataIso,
                filialId = filialId,
                jogoId = jogoId,
                nomeImagem = nomeImagemPredefinido, // <--- Aqui está o segredo
                linkInscricao = ""
            )

            val sucesso = repository.cadastrarEvento(novoEvento)
            if (sucesso) {
                onSuccess()
            }
            isLoading.value = false
        }
    }
    fun carregarParticipantes(eventoId: Long) {
        viewModelScope.launch {
            carregandoParticipantes.value = true
            try {
                // 2. O repository.buscarParticipantes(eventoId)
                // também deve ser alterado para retornar List<String> no arquivo do Repository!
                val lista = repository.buscarParticipantes(eventoId)

                participantes.clear()
                participantes.addAll(lista) // Agora lista de String entra em lista de String
            } catch (e: Exception) {
                Log.e("EVENTO_VM", "Erro ao carregar participantes: ${e.message}")
            } finally {
                carregandoParticipantes.value = false
            }
        }
    }

    // Mantida por compatibilidade, mas agora usa a lógica de upload se necessário
    fun cadastrarNovoEvento(
        titulo: String,
        descricao: String,
        dataIso: String,
        filialId: Long,
        jogoId: Long,
        jogoNome: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            val dto = EventoRequestDTO(
                titulo = titulo,
                descricao = descricao,
                dataHora = dataIso,
                linkInscricao = "",
                filialId = filialId,
                jogoId = jogoId,
                nomeImagem = "semanal_${jogoNome.lowercase().replace(" ", "_")}.jpg"
            )
            if (repository.cadastrarEvento(dto)) onSuccess()
            isLoading.value = false
        }
    }
}