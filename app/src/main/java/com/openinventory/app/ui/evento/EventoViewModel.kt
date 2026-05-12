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
import android.graphics.Bitmap
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
    fun excluirEvento(id: Long) {
        viewModelScope.launch {
            isLoading.value = true
            val sucesso = repository.excluirEvento(id)
            if (sucesso) {
                // Remove da lista local para a UI atualizar na hora sem precisar recarregar tudo
                listaEventos.removeAll { it.id == id }
                Log.d("EVENTO_VM", "Evento $id excluído com sucesso")
            } else {
                mensagemErro.value = "Não foi possível excluir o evento."
            }
            isLoading.value = false
        }
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
        nomeImagemPredefinido: String, // ex: imagem do jogo se não tiver upload
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true

            // 1. DEFINIMOS O NOME AQUI E AGORA
            val tituloLimpo = titulo.trim()
            val nomeBase = if (isSemanal) {
                "semanal_${jogoNome.trim().lowercase().replace(" ", "_")}"
            } else {
                tituloLimpo.lowercase().replace(" ", "_")
            }.replace(":", "").replace("/", "")

            // Este é o nome que vamos mandar para o banco, aconteça o que acontecer
            val nomeFinalComExtensao = "$nomeBase.jpg"

            // 2. TENTAMOS O UPLOAD (Fogo e Esqueça)
            if (uri != null) {
                val arquivoParaUpload = comprimirImagem(context, uri)
                if (arquivoParaUpload != null) {
                    // Chamamos o upload, mas não travamos o nomeFinal se der erro
                    try {
                        // Passamos o nomeBase que queremos que o servidor use
                        repository.uploadImagem(arquivoParaUpload, nomeBase)
                        Log.d("EVENTO", "Tentativa de upload enviada: $nomeFinalComExtensao")
                    } catch (e: Exception) {
                        Log.e("EVENTO", "Upload falhou, mas seguiremos com o cadastro: ${e.message}")
                    } finally {
                        arquivoParaUpload.delete()
                    }
                }
            }

            // 3. CRIAR O EVENTO (Usando o nome que decidimos no passo 1)
            val novoEvento = EventoRequestDTO(
                titulo = tituloLimpo,
                descricao = descricao,
                dataHora = dataIso,
                filialId = filialId,
                jogoId = jogoId,
                nomeImagem = if (uri != null) nomeFinalComExtensao else nomeImagemPredefinido,
                linkInscricao = ""
            )

            Log.d("EVENTO_ENVIO", "Cadastrando evento com imagem: ${novoEvento.nomeImagem}")

            val sucesso = repository.cadastrarEvento(novoEvento)
            if (sucesso) onSuccess()

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

    private fun comprimirImagem(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Cria um arquivo temporário para a imagem comprimida
            val compressedFile = File(context.cacheDir, "temp_event_image.jpg")
            val outputStream = FileOutputStream(compressedFile)

            // 70 é o nível de qualidade (0-100). Reduz de MBs para KBs.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            outputStream.flush()
            outputStream.close()
            compressedFile
        } catch (e: Exception) {
            Log.e("EVENTO_VM", "Erro ao comprimir: ${e.message}")
            null
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