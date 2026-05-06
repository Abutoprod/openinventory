package com.openinventory.app.ui.comanda

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import  kotlinx.coroutines.flow.combine
import java.util.Date
import com.openinventory.app.ui.sale.SaleModel
import com.google.firebase.firestore.Query
import android.util.Log
import com.openinventory.app.core.config.CompanyConstants
import com.openinventory.app.data.database.entity.OrderEntity
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.repository.OrderRepository
import com.openinventory.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import com.openinventory.app.ui.sale.SaleItem
import com.google.firebase.firestore.ListenerRegistration

class OrderViewModel(
    private val repository: OrderRepository,
    private val productRepository: ProductRepository,
    private val db: com.google.firebase.firestore.FirebaseFirestore
) : ViewModel() {

    //private val db = Firebase.firestore
    private var isHistoryLoaded = false

    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()

    private val _availableCustomers = MutableStateFlow<List<String>>(emptyList())
    val availableCustomers: StateFlow<List<String>> = _availableCustomers.asStateFlow()
    private var salesListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private val _tempItems = MutableStateFlow<List<Triple<String, String, Double>>>(emptyList())
    val tempItems: StateFlow<List<Triple<String, String, Double>>> = _tempItems.asStateFlow()

    private val _confirmedItems = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val confirmedItems = _confirmedItems.asStateFlow()


    private val _salesHistory = MutableStateFlow<List<SaleModel>>(emptyList())
    val salesHistory: StateFlow<List<SaleModel>> = _salesHistory.asStateFlow()

    // Estados para identificação na Venda Rápida
    var quickSaleCustomerName by mutableStateOf("")
    var quickSaleCustomerCpf by mutableStateOf("")

    // Fluxo de produtos do estoque vindo do Room
    val inventoryProducts: StateFlow<List<ProductEntity>> = productRepository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        observeFirebaseOrders(CompanyConstants.currentStoreId)
    }

    fun loadHistoryIfNeeded() {
        // Cancela listeners antigos para não duplicar dados ou filtrar errado ao trocar de filial
        salesListener?.remove()
        ordersListener?.remove()

        val currentStore = CompanyConstants.currentStoreId
        println("DEBUG: Carregando histórico para a filial: $currentStore")

        val quickSalesFlow = MutableStateFlow<List<SaleModel>>(emptyList())
        val finishedOrdersFlow = MutableStateFlow<List<SaleModel>>(emptyList())

        // 1. Vendas Diretas (QUICK_SALE)
        salesListener = db.collection("sales")
            .whereEqualTo("storeId", currentStore) // FILTRO POR FILIAL
            //.orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.i("ERROR","ERRO SALES: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    SaleModel(
                        customerName = doc.getString("customerName") ?: "Venda Rápida",
                        total = doc.getDouble("total") ?: 0.0,
                        timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date(),
                        items = itemsRaw.map {
                            SaleItem(it["name"] as? String ?: "", (it["price"] as? Number)?.toDouble() ?: 0.0)
                        },
                        cpf = doc.getString("customerCpf") ?: ""
                    )
                } ?: emptyList()
                quickSalesFlow.value = list
            }

        // 2. Comandas Finalizadas
        ordersListener = db.collection("orders")
            .whereEqualTo("storeId", currentStore) // FILTRO POR FILIAL
            .whereEqualTo("status", "FINISHED")
            .orderBy("closedAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("ERRO ORDERS: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val itemsSummary = doc.get("itemsSummary") as? List<Map<String, Any>> ?: emptyList()
                    SaleModel(
                        customerName = "Comanda: ${doc.getString("customerName") ?: "Mesa"}",
                        total = doc.getDouble("totalAmount") ?: 0.0,
                        timestamp = doc.getTimestamp("closedAt")?.toDate() ?: Date(),
                        items = itemsSummary.map {
                            SaleItem(it["name"] as? String ?: "", (it["price"] as? Number)?.toDouble() ?: 0.0)
                        },
                        cpf = ""
                    )
                } ?: emptyList()
                finishedOrdersFlow.value = list
            }

        // 3. Combina os dois fluxos
        viewModelScope.launch {
            combine(quickSalesFlow, finishedOrdersFlow) { quick, finished ->
                (quick + finished).sortedByDescending { it.timestamp }
            }.collect { combinedList ->
                _salesHistory.value = combinedList
            }
        }
    }
    /*fun loadHistoryIfNeeded() {
        // Se já carregou uma vez, não gasta consulta de novo à toa
        if (!isHistoryLoaded) {
            observeSalesHistory()
            isHistoryLoaded = true
        }
    }*/

    // --- LÓGICA DE ESCUTA DO FIREBASE ---

    fun observeFirebaseOrders(storeId: String) {

        db.collection("orders")
            .whereEqualTo("status", "OPEN")
            .whereEqualTo("storeId", storeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val ordersList = snapshot?.documents?.mapNotNull { doc ->
                    OrderEntity(
                        orderId = doc.id,
                        customerName = doc.getString("customerName") ?: "Desconhecido",
                        isOpen = doc.getString("status") == "OPEN",
                        totalAmount = doc.getDouble("totalAmount") ?: 0.0
                    )
                } ?: emptyList()
                _orders.value = ordersList
            }
    }

    // --- GERENCIAMENTO DE ITENS (CARRINHO TEMPORÁRIO) ---

    fun addToTempList(code: String, name: String, price: Double) {
        _tempItems.value = _tempItems.value + Triple(code, name, price)
    }

    fun removeFromTempList(index: Int) {
        val newList = _tempItems.value.toMutableList()
        if (index in newList.indices) {
            newList.removeAt(index)
            _tempItems.value = newList
        }
    }

    fun clearTempList() {
        _tempItems.value = emptyList()
    }

    // --- OPERAÇÕES DE COMANDA ---

    fun createNewOrder(customerName: String, storeId: String) {
        val newOrder = hashMapOf(
            "customerName" to customerName,
            "status" to "OPEN",
            "openedAt" to FieldValue.serverTimestamp(),
            "totalAmount" to 0.0,
            "storeId" to storeId
        )
        db.collection("orders").add(newOrder)
        //db.collection("customers").document(customerName).set(mapOf("name" to customerName))
    }

    fun confirmOrderItems(orderId: String) {
        val itemsToConfirm = _tempItems.value
        if (itemsToConfirm.isEmpty()) return

        val additionalTotal = itemsToConfirm.sumOf { it.third }

        viewModelScope.launch {
            val orderRef = db.collection("orders").document(orderId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val currentTotal = snapshot.getDouble("totalAmount") ?: 0.0
                val newTotal = currentTotal + additionalTotal

                // 1. Atualiza o totalAmount da comanda somando o anterior + novos
                transaction.update(orderRef, "totalAmount", newTotal)

                // 2. Adiciona os itens na subcoleção para o histórico
                itemsToConfirm.forEach { item ->
                    val itemData = hashMapOf(
                        "productCode" to item.first,
                        "name" to item.second,
                        "price" to item.third,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    transaction.set(orderRef.collection("items").document(), itemData)
                }
                null
            }.addOnSuccessListener {
                // Limpa a lista temporária apenas após o sucesso no Firebase
                _tempItems.value = emptyList()
            }.addOnFailureListener { e ->
                Log.e("OrderViewModel", "Erro ao confirmar itens", e)
            }
        }
    }

    fun loadConfirmedItems(orderId: String) {
        db.collection("orders").document(orderId).collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.map { doc ->
                    (doc.getString("name") ?: "") to (doc.getDouble("price") ?: 0.0)
                } ?: emptyList()
                _confirmedItems.value = items
            }
    }

    // --- LÓGICA DE RECIBO E FINALIZAÇÃO ---

    fun buildReceiptText(items: List<Pair<String, Double>>, total: Double, name: String?, cpf: String?): String {
        val sb = StringBuilder()
        sb.append("${CompanyConstants.RAZAO_SOCIAL}\n")
        sb.append("CNPJ: ${CompanyConstants.CNPJ}\n")
        sb.append("${CompanyConstants.ENDERECO}\n")
        sb.append("-".repeat(32) + "\n")
        sb.append("      RECIBO NÃO FISCAL\n")
        sb.append("-".repeat(32) + "\n")

        if (!name.isNullOrBlank()) sb.append("CLIENTE: $name\n")
        if (!cpf.isNullOrBlank()) sb.append("CPF: $cpf\n")

        sb.append("-".repeat(32) + "\n")
        sb.append("ITEM            QTD    PREÇO\n")

        items.forEach { (desc, price) ->
            val truncatedDesc = if (desc.length > 15) desc.take(15) else desc.padEnd(15)
            val line = truncatedDesc + "  1x   R$${String.format("%.2f", price)}"
            sb.append("$line\n")
        }

        sb.append("-".repeat(32) + "\n")
        sb.append("TOTAL: R$ ${String.format("%.2f", total)}\n")
        sb.append("-".repeat(32) + "\n")
        val date = java.text.SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        sb.append("Data: $date\n")
        sb.append("${CompanyConstants.MENSAGEM_RODAPE}\n")

        return sb.toString()
    }

    fun finishOrderWithReceipt(order: OrderEntity, onComplete: (String) -> Unit) {
        val dateId = java.text.SimpleDateFormat("yyyy_MM_dd", java.util.Locale.getDefault()).format(java.util.Date())
        val currentStore = CompanyConstants.currentStoreId // Pega a loja atual

        // CRITICAL: Usa o mesmo padrão de ID da Venda Rápida
        val statsDocId = "${dateId}_$currentStore"
        val statsRef = db.collection("daily_stats").document(statsDocId)
        val orderRef = db.collection("orders").document(order.orderId)

        // Cálculo do lucro real da comanda
        val totalProfit = _confirmedItems.value.sumOf { item ->
            val product = inventoryProducts.value.find { it.description == item.first }
            val cost = product?.purchasePrice ?: 0.0
            item.second - cost
        }

        db.runTransaction { transaction ->
            val statsSnap = transaction.get(statsRef)

            // 0.5 PARA SALVAR OS ITENS NA COMANDA (Para o histórico)
            val summary = _confirmedItems.value.map {
                mapOf("name" to it.first, "price" to it.second)
            }
            transaction.update(orderRef, "itemsSummary", summary)

            // 1. Atualizar Ranking de Produtos (Top Products)
            _confirmedItems.value.forEach { (name, price) ->
                val product = inventoryProducts.value.find { it.description == name }
                val productName = product?.description ?: name

                val topProductRef = statsRef.collection("top_products").document(productName)
                transaction.set(topProductRef,
                    mapOf(
                        "name" to name,
                        "quantity" to FieldValue.increment(1),
                        "totalRevenue" to FieldValue.increment(price)
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }

            // 2. Fechar a comanda
            transaction.update(orderRef, "status", "FINISHED", "closedAt", FieldValue.serverTimestamp())

            // 3. Atualizar Dashboard (IGUAL À VENDA RÁPIDA)
            val saleTotal = order.totalAmount
            if (!statsSnap.exists()) {
                transaction.set(statsRef, hashMapOf(
                    "totalRevenue" to saleTotal,
                    "totalProfit" to totalProfit,
                    "count" to 1,
                    "storeId" to currentStore, // ADICIONADO PARA O FILTRO
                    "dateId" to dateId         // ADICIONADO PARA O FILTRO
                ))
            } else {
                transaction.update(statsRef,
                    "totalRevenue", FieldValue.increment(saleTotal),
                    "totalProfit", FieldValue.increment(totalProfit),
                    "count", FieldValue.increment(1),
                    "storeId", currentStore, // GARANTE QUE O CAMPO EXISTE
                    "dateId", dateId         // GARANTE QUE O CAMPO EXISTE
                )
            }
            null
        }.addOnSuccessListener {
            onComplete(buildReceiptText(_confirmedItems.value, order.totalAmount, order.customerName, null))
        }
    }
    private fun observeSalesHistory() {
        val currentStore = CompanyConstants.currentStoreId
        val quickSalesFlow = MutableStateFlow<List<SaleModel>>(emptyList())
        val finishedOrdersFlow = MutableStateFlow<List<SaleModel>>(emptyList())

        // Listener para Vendas Diretas
        db.collection("sales")
            .whereEqualTo("storeId", currentStore)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Erro em sales: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    SaleModel(
                        // Se customerName for vazio no banco, define como "Venda Rápida"
                        customerName = doc.getString("customerName").takeIf { !it.isNullOrBlank() } ?: "Venda Rápida",
                        total = doc.getDouble("total") ?: 0.0,
                        timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date(),
                        items = itemsRaw.map {
                            SaleItem(
                                name = it["name"] as? String ?: "",
                                price = (it["price"] as? Number)?.toDouble() ?: 0.0
                            )
                        },
                        cpf = doc.getString("customerCpf") ?: ""
                    )
                } ?: emptyList()
                quickSalesFlow.value = list
            }

        // Listener para Comandas Finalizadas
        db.collection("orders")
            .whereEqualTo("storeId", currentStore)
            .whereEqualTo("status", "FINISHED")
            .orderBy("closedAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Erro em orders: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val itemsRaw = doc.get("itemsSummary") as? List<Map<String, Any>> ?: emptyList()
                    SaleModel(
                        customerName = "Comanda: ${doc.getString("customerName") ?: "Mesa"}",
                        total = doc.getDouble("totalAmount") ?: 0.0,
                        timestamp = doc.getTimestamp("closedAt")?.toDate() ?: Date(),
                        items = itemsRaw.map { SaleItem(it["name"] as? String ?: it["description"] as? String ?: "", (it["price"] as? Number)?.toDouble() ?: 0.0) },
                        cpf = ""
                    )
                } ?: emptyList()
                finishedOrdersFlow.value = list
            }

        viewModelScope.launch {
            combine(quickSalesFlow, finishedOrdersFlow) { quick, finished ->
                (quick + finished).sortedByDescending { it.timestamp }
            }.collect { combinedList ->
                _salesHistory.value = combinedList
            }
        }
    }

    fun finishQuickSale(onComplete: (String) -> Unit) {
        val items = _tempItems.value
        if (items.isEmpty()) return

        val totalRevenue = items.sumOf { it.third }

        // Pegamos o ID da loja atual conforme sua lógica de 'createNewOrder'
        val currentStore = CompanyConstants.currentStoreId

        val totalProfit = items.sumOf { item ->
            val product = inventoryProducts.value.find { it.code == item.first }
            val cost = product?.purchasePrice ?: 0.0
            item.third - cost
        }

        val dateId = java.text.SimpleDateFormat("yyyy_MM_dd", java.util.Locale.getDefault()).format(java.util.Date())

        // Criamos o ID do documento combinando DATA e STOREID para não misturar as lojas
        val statsDocId = "${dateId}_$currentStore"
        val statsRef = db.collection("daily_stats").document(statsDocId)

        db.runTransaction { transaction ->
            val statsSnap = transaction.get(statsRef)

            // Atualizar Ranking de Produtos (Top Products)
            items.forEach { (_, productName, productPrice) ->
                val topProductRef = statsRef.collection("top_products").document(productName)
                transaction.set(topProductRef, hashMapOf(
                    "name" to productName,
                    "quantity" to FieldValue.increment(1),
                    "totalRevenue" to FieldValue.increment(productPrice)
                ), com.google.firebase.firestore.SetOptions.merge())
            }

            // Salva a venda no histórico global com o storeId
            val saleRef = db.collection("sales").document()
            val saleData = hashMapOf(
                "items" to items.map { mapOf("name" to it.second, "price" to it.third) },
                "total" to totalRevenue,
                "storeId" to currentStore, // REGISTRANDO O STOREID DA VENDA
                "profit" to totalProfit,
                "customerName" to quickSaleCustomerName,
                "customerCpf" to quickSaleCustomerCpf,
                "timestamp" to FieldValue.serverTimestamp(),
                "type" to "QUICK_SALE"
            )
            transaction.set(saleRef, saleData)

            // Atualiza os totais do dia com as propriedades para o filtro do Dashboard
            if (!statsSnap.exists()) {
                transaction.set(statsRef, hashMapOf(
                    "totalRevenue" to totalRevenue,
                    "totalProfit" to totalProfit,
                    "count" to 1,
                    "storeId" to currentStore, // PROPRIEDADE PARA O DASHBOARD FILTRAR
                    "dateId" to dateId         // PROPRIEDADE PARA O DASHBOARD FILTRAR
                ))
            } else {
                transaction.update(statsRef,
                    "totalRevenue", FieldValue.increment(totalRevenue),
                    "totalProfit", FieldValue.increment(totalProfit),
                    "count", FieldValue.increment(1),
                    "storeId", currentStore,
                    "dateId", dateId
                )
            }
            null
        }.addOnSuccessListener {
            val receipt = buildReceiptText(items.map { it.second to it.third }, totalRevenue, quickSaleCustomerName, quickSaleCustomerCpf)
            clearTempList()
            quickSaleCustomerName = ""
            quickSaleCustomerCpf = ""
            onComplete(receipt)
        }
    }
}