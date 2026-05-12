package com.openinventory.app.ui.ponto
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.data.repository.EventoRepository
import com.openinventory.app.data.repository.PontosRepository
import com.openinventory.app.service.JogoResponseDTO
import com.openinventory.app.service.RankingDTO
import com.openinventory.app.service.UsuarioResponse // Ajustado para o seu novo nome
import com.openinventory.app.service.LancamentoDTO
import kotlinx.coroutines.launch
import kotlinx.coroutines.joinAll
import java.util.Calendar

class PontosViewModel(
    private val pontosRepository: PontosRepository,
    private val eventoRepository: EventoRepository
) : ViewModel() {

    var jogos = mutableStateListOf<JogoResponseDTO>()
    var ranking = mutableStateListOf<RankingDTO>()
    var clientes = mutableStateListOf<UsuarioResponse>() // Lista de Clientes

    var jogoSelecionado by mutableStateOf<JogoResponseDTO?>(null)
    var isLoading by mutableStateOf(false)
    var isLancando by mutableStateOf(false)

    fun inicializar(filialId: Long) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Busca jogos e clientes em paralelo
                val jobJogos = launch {
                    val lista = eventoRepository.buscarJogos()
                    jogos.clear()
                    jogos.addAll(lista)
                }
                val jobClientes = launch {
                    val lista = pontosRepository.buscarClientes() // Chamando sua nova função
                    clientes.clear()
                    clientes.addAll(lista)
                }

                joinAll(jobJogos, jobClientes)

                if (jogos.isNotEmpty() && jogoSelecionado == null) {
                    selecionarJogo(jogos.first(), filialId)
                }
            } catch (e: Exception) {
                Log.e("PontosVM", "Erro ao inicializar: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun selecionarJogo(jogo: JogoResponseDTO, filialId: Long) {
        jogoSelecionado = jogo
        atualizarRanking(filialId)
    }

    fun atualizarRanking(filialId: Long, mes: Int? = null, ano: Int? = null) {
        val jogoId = jogoSelecionado?.id ?: return

        // Se você quiser que o padrão do APP seja sempre o mês atual:
        val mesFinal = mes ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
        val anoFinal = ano ?: Calendar.getInstance().get(Calendar.YEAR)

        viewModelScope.launch {
            isLoading = true
            try {
                val lista = pontosRepository.buscarRanking(jogoId, filialId, mesFinal, anoFinal)
                ranking.clear()
                ranking.addAll(lista)
            } finally {
                isLoading = false
            }
        }
    }

    fun lancarPontos(
        usuarioId: Long,
        pontos: Int,
        descricao: String,
        filialId: Long,
        jogoId: Long,
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {
            isLancando = true
            val sucesso = pontosRepository.lancarPontos(
                LancamentoDTO(usuarioId, jogoId, filialId, pontos, descricao)
            )
            if (sucesso) {
                atualizarRanking(filialId)
                onSuccess()
            }
            isLancando = false
        }
    }
}