package com.openinventory.app.ui.comanda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openinventory.app.data.repository.ComandaRepository
import com.openinventory.app.service.ComandaResponseDTO
import com.openinventory.app.service.UsuarioResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.openinventory.app.service.ProductResponse
import android.util.Log
class ComandaViewModel(private val repository: ComandaRepository) : ViewModel() {


    private val _comandas = MutableStateFlow<List<ComandaResponseDTO>>(emptyList())
    val comandas = _comandas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _produtosLoja = MutableStateFlow<List<ProductResponse>>(emptyList())
    val produtosLoja = _produtosLoja.asStateFlow()
    // --- Estados dos Filtros ---
    private val _filtroStatus = MutableStateFlow<String?>("abertas") // Padrão inicial
    val filtroStatus = _filtroStatus.asStateFlow()

    private val _clienteSelecionado = MutableStateFlow<UsuarioResponse?>(null)
    val clienteSelecionado = _clienteSelecionado.asStateFlow()

    // Sugestões para o campo de busca de cliente
    private val _sugestoesClientes = MutableStateFlow<List<UsuarioResponse>>(emptyList())
    val sugestoesClientes = _sugestoesClientes.asStateFlow()
    private var listaCompletaClientes: List<UsuarioResponse> = emptyList()
    private val _comandaDetalhada = MutableStateFlow<ComandaResponseDTO?>(null)
    val comandaDetalhada = _comandaDetalhada.asStateFlow()
    init {
        // Carrega a lista assim que o ViewModel é criado para que a busca funcione
        carregarUsuariosParaBusca()
    }
    fun carregarUsuariosParaBusca() {
        viewModelScope.launch {
            try {
                // O repository já tem o método getUsuarios() que você mandou!
                val usuarios = repository.getUsuarios()
                listaCompletaClientes = usuarios
                Log.d("ComandaViewModel", "Clientes carregados para busca: ${usuarios.size}")
            } catch (e: Exception) {
                Log.e("ComandaViewModel", "Erro ao carregar clientes: ${e.message}")
            }
        }
    }

    fun carregarComandas(filialId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Agora passamos status E clienteSelecionado para o Repository
                val lista = repository.getComandasFiltradas(
                    filialId = filialId,
                    status = _filtroStatus.value,
                    clienteId = _clienteSelecionado.value?.id // Filtro por cliente aqui!
                )
                _comandas.value = lista
            } catch (e: Exception) {
                Log.e("ViewModel", "Erro ao carregar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun aplicarFiltroStatus(status: String?, filialId: Long) {
        _filtroStatus.value = status
        carregarComandas(filialId)
    }

    fun aplicarFiltroCliente(cliente: UsuarioResponse?, filialId: Long) {
        _clienteSelecionado.value = cliente
        carregarComandas(filialId)
    }
    fun carregarProdutosDaLoja(filialId: Long) {
        viewModelScope.launch {
            val produtos = repository.getProdutosPorFilial(filialId)
            _produtosLoja.value = produtos
        }
    }

    fun filtrarClientesParaBusca(query: String) {
        val t = query.trim()
        _sugestoesClientes.value = if (t.length < 2) emptyList()
        else listaCompletaClientes.filter { it.nome.contains(t, ignoreCase = true) }
    }

    fun alterarFiltroStatus(filialId: Long, novoStatus: String?) {
        _filtroStatus.value = novoStatus
        carregarComandas(filialId)
    }

    fun abrirNovaComanda(usuarioId: Long, filialId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val sucesso = repository.abrirComanda(usuarioId, filialId)
            if (sucesso) {
                carregarComandas(filialId) // Atualiza a lista na tela
            }
            _isLoading.value = false
        }
    }
    // Em ComandaViewModel.kt

    fun lancarProdutoNaComanda(comandaId: Long, produto: ProductResponse, quantidade: Int, filialId: Long) {
        viewModelScope.launch {
            // Passamos o produto.code (String) que é o que o seu Java busca no banco
            val sucesso = repository.adicionarItem(comandaId, produto.code, quantidade)
            if (sucesso) {
                carregarComandas(filialId)
            }
        }
    }
    fun fecharComanda(comandaId: Long, filialId: Long) {
        viewModelScope.launch {
            _isLoading.value = true // Use _isLoading que já existe no seu topo do arquivo
            try {
                val response = repository.fecharComanda(comandaId)

                if (response.isSuccessful) {
                    carregarComandas(filialId)
                } else {
                    // Corrigido: code é uma propriedade, não uma função
                    Log.e("ComandaViewModel", "Erro ao fechar: ${response.code()}")
                }
                carregarComandas(filialId)
            } catch (e: Exception) {
                Log.e("ComandaViewModel", "Falha na rede: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun selecionarClienteFiltro(usuario: UsuarioResponse?) {
        _clienteSelecionado.value = usuario
        // Toda vez que mudar o cliente, recarregamos a lista
        // idFilial aqui deve ser o ID que você já tem guardado ou passado por parâmetro
    }
    fun carregarDetalhes(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val detalhe = repository.getDetalhesComanda(id)
            _comandaDetalhada.value = detalhe
            _isLoading.value = false
        }
    }

    fun limparDetalhes() {
        _comandaDetalhada.value = null
    }
}