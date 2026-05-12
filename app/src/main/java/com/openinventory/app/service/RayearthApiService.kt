package com.openinventory.app.service

import android.R
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
import retrofit2.http.*
import okhttp3.ResponseBody
import okhttp3.RequestBody
import okhttp3.MultipartBody


// --- MODELOS DE DADOS (Colocamos aqui para evitar erros de import) ---

data class LoginRequest(
    val email: String,
    val senha: String
)

data class LoginResponse(
    val token: String,
    val role: String
)

data class FilialResponse(
    val id: Long,
    val nome: String,
    val cidade: String,
    val endereco: String, // Adicionado
    val ativo: Boolean    // Adicionado para filtragem
)

data class ProductResponse(
    val id: Long, // ADICIONE ESTA LINHA AQUI
    @SerializedName("codigo") val code: String,
    @SerializedName("descricao") val description: String,
    @SerializedName("precoVenda") val price: Double,
    @SerializedName("precoCompra") val purchasePrice: Double,
    @SerializedName("quantidade") val quantity: Int,
    @SerializedName("categoria") val category: String,
    @SerializedName("filial") val filial: FilialResponse // Para pegar o nome da filial
)

data class ProductDTO(
    val codigo: String,
    val descricao: String,
    val precoCompra: Double,
    val precoVenda: Double,
    val quantidade: Int,
    val categoria: String,
    val filialId: Long
)
// O QUE O JAVA DEVOLVE (Response Body)
data class ProductResponsecreate(
    val id: Long,
    val codigo: String,
    val descricao: String,
    val precoCompra: Double,
    val precoVenda: Double,
    val quantidade: Int,
    val categoria: String,
    val filial: FilialResponse // Objeto completo como mostraste no JSON
)
data class ComandaResponse(
    val id: Long,
    val nomeCliente: String,
    val mesa: String?,
    val status: String, // "ABERTA" ou "FECHADA"
    val itens: List<ItemComandaResponse>,
    val total: Double,
    val dataAbertura: String
)

data class ItemComandaResponse(
    val id: Long,
    val produtoNome: String,
    val quantidade: Int,
    val precoUnitario: Double,
    val subtotal: Double
)
data class ItemComandaDTO(
    val id: Long,
    val produtoNome: String,
    val quantidade: Int,
    val precoCompra: Double,  // Valor que adicionamos para o custo
    val precoUnitario: Double // Valor de venda
)

data class ComandaResponseDTO(
    val id: Long,
    val nomeCliente: String,
    val valorTotal: Double,
    val aberta: Boolean,
    val dataAbertura: String, // O LocalDateTime do Java vira String no JSON
    val itens: List<ItemComandaDTO>
)
// DTO para abrir uma nova comanda
data class AbrirComandaDTO(
    val nomeCliente: String,
    val mesa: String?,
    val filialId: Long
)
data class UsuarioResponse(
    val id: Long,
    val nome: String,
    val email: String
)

// DTO para adicionar item
data class AdicionarItemDTO(
    val produtoId: Long,
    val quantidade: Int
)
data class UpdateStockRequest(
    val sku: String,
    val quantity: Int,
    val storeId: String
)
data class EventoDTO(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val dataHora: String,
    val urlImagem: String,
    val linkInscricao: String,
    val jogoNome: String,
    val filialNome: String
)

data class JogoResponseDTO(val id: Long, val nome: String)
data class EventoRequestDTO(
    val titulo: String,
    val descricao: String,
    val dataHora: String, // Formato ISO "yyyy-MM-ddTHH:mm:ss"
    val linkInscricao: String,
    val filialId: Long,
    val jogoId: Long,
    val nomeImagem: String
)
data class ItemVendaDTO(
    @SerializedName("codigoProduto")
    val id: Long, // Mude de codigoProduto para id
    val quantidade: Int
)

data class VendaRapidaRequest(
    val filialId: Long,
    val clienteId: Long, // Adicionado
    val itens: List<ItemVendaDTO>
)
data class DashboardResponse(
    val totalRecebido: Double,
    val totalCusto: Double,
    val produtosConsumidos: List<ProdutoGraficoDTO>
)

data class ProdutoGraficoDTO(
    val nome: String,
    val quantidade: Int
)

data class ParticipanteDTO(
    val id: Long,
    val nome: String,
    val email: String,
    val telefone: String? = null
)

data class RankingDTO(
    val usuarioId: Long,
    val nome: String,
    @SerializedName("totalPontos") val pontos: Int
)

data class LancamentoDTO(
    val usuarioId: Long,
    val jogoId: Long,
    val filialId: Long,
    val pontos: Int,

    val descricao: String
)

// --- A INTERFACE DA API ---

interface RayearthApiService {
    // Se o seu backend não usa o prefixo /api, deixe apenas "auth/login" ou "login"
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/api/usuarios")
    suspend fun listarUsuarios(
        @Header("Authorization") token: String // Adicione isso
    ): Response<List<UsuarioResponse>>

    @GET("/api/filiais")
    suspend fun getFiliais(
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<List<FilialResponse>>

    @GET("/api/estoque")
    suspend fun getProdutosPorFilial(
       // @Header("Authorization") token: String?, // EXATAMENTE ASSIM
        @Query("filialId") filialId: String,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<List<ProductResponse>>

    // No RayearthApiService.kt
    @POST("/api/estoque")
    suspend fun postProdutosPorFilial(
        @Header("Authorization") token: String?,
        @Body produto: ProductDTO, // O NOME AQUI DEVE SER 'produto'
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<ProductResponse>

    @PUT("/api/estoque/{id}")
    suspend fun atualizarProduto(
        @Header("Authorization") token: String?,
        @Path("id") id: Long,
        @Body produto: ProductDTO,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<ProductResponse>

    /// COMANDAAS
    // ADMIN: Listar todas com filtro de filial (Obrigatório no seu Java)
    @GET("/api/comandas")
    suspend fun listarTodasComandas(
        @Header("Authorization") token: String?,
        @Query("filialId") filialId: Long,
        @Query("status") status: String?,     // "abertas", "fechadas" ou null
        @Query("clienteId") clienteId: Long?,  // ID do cliente específico
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<List<ComandaResponseDTO>>


    // 1. ABRIR COMANDA: @PostMapping("/abrir/{usuarioId}") com filialId via RequestParam
    @POST("/api/comandas/abrir/{usuarioId}")
    suspend fun abrirComanda(
        @Header("Authorization") token: String?,
        @Path("usuarioId") usuarioId: Long,
        @Query("filialId") filialId: Long,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<Any>


    // 2. ADICIONAR ITEM: @PostMapping("/{comandaId}/item") com params via RequestParam
    // No RayearthApiService.kt, altere para:
    @POST("/api/comandas/{comandaId}/item")
    suspend fun adicionarItem(
        @Header("Authorization") token: String?,
        @Path("comandaId") comandaId: Long,
        @Query("codigoProduto") codigoProduto: String, // Java espera o CÓDIGO (String), não o ID
        @Query("quantidade") quantidade: Int,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<ResponseBody>

    @GET("/api/comandas/{id}")
    suspend fun buscarComandaPorId(
        @Header("Authorization") token: String?,
        @Path("id") id: Long,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<ComandaResponseDTO>


    // 3. FECHAR COMANDA: @PutMapping("/{comandaId}/fechar")
    @PUT("/api/comandas/{comandaId}/fechar")
    suspend fun fecharComanda(
        @Header("Authorization") token: String, // O "Bearer ..." vai aqui
        @Path("comandaId") comandaId: Long,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<String>
    @POST("/api/comandas/venda-rapida") // Ajustado com hífen conforme o Postman
    suspend fun realizarVendaRapida(
        @Header("Authorization") token: String,
        @Body request: VendaRapidaRequest,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ):  Response<okhttp3.ResponseBody>

    @POST("/api/eventos")
    suspend fun criarEvento(
        @Header("Authorization") token: String,
        @Body evento: EventoRequestDTO,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<okhttp3.ResponseBody>

    @GET("/api/jogos")
    suspend fun listarJogos(): List<JogoResponseDTO>

    @GET("/api/eventos")
    suspend fun listarEventos(): List<EventoDTO>

    @GET("/api/eventos/{eventoId}/participantes-pre-cadastrados")
    suspend fun listarParticipantes(
        @Path("eventoId") eventoId: Long
    ):  List<String>

    @Multipart
    @POST("/api/arquivos/upload")
    suspend fun uploadImagem(
        @Header("Authorization") token: String,
        @Part imagem: MultipartBody.Part,
        @Part("nome") nome: RequestBody? = null, // Adicionamos o nome aqui
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<String>


    @GET("/api/pontos/ranking")
    suspend fun consultarRanking(
        @Header("Authorization") token: String,
        @Query("jogoId") jogoId: Long,
        @Query("filialId") filialId: Long,
        @Query("mes") mes: Int? = null, // Novo
        @Query("ano") ano: Int? = null, // Novo
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): List<RankingDTO>

    @POST("/api/pontos")
    suspend fun lancarPontos(
        @Header("Authorization") token: String, // Adicionado
        @Body dados: LancamentoDTO,
        @Header("ngrok-skip-browser-warning") skip: String = "true"
    ): Response<okhttp3.ResponseBody>

    @DELETE("/api/eventos/{id}")
    suspend fun excluirEvento(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    @POST("/api/usuarios/esqueci-senha")
    suspend fun solicitarCodigoSenha(@Body payload: Map<String, String>): Response<Unit>

    @POST("/api/usuarios/redefinir-senha")
    suspend fun redefinirSenha(@Body payload: Map<String, String>): Response<Unit>
}