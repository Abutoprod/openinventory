package com.openinventory.app.ui.comanda

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import  kotlinx.coroutines.flow.combine
import java.util.Date
import com.openinventory.app.ui.sale.SaleModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
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

class OrderViewModel(
    private val repository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val db = Firebase.firestore
    private var isHistoryLoaded = false

    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()

    private val _availableCustomers = MutableStateFlow<List<String>>(emptyList())
    val availableCustomers: StateFlow<List<String>> = _availableCustomers.asStateFlow()

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
        observeFirebaseOrders()
       // refreshCustomers()
        //observeCustomers()
        //observeSalesHistory()
    }
    fun loadHistoryIfNeeded() {
        // Se já carregou uma vez, não gasta consulta de novo à toa
        if (!isHistoryLoaded) {
            observeSalesHistory()
            isHistoryLoaded = true
        }
    }

    // --- LÓGICA DE ESCUTA DO FIREBASE ---

    fun observeFirebaseOrders() {
        db.collection("orders")
            .whereEqualTo("status", "OPEN")
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

    private fun observeCustomers() {
        db.collection("customers")
            .addSnapshotListener { snapshot, _ ->
                val names = snapshot?.documents?.mapNotNull { it.getString("name") } ?: emptyList()
                _availableCustomers.value = names.sorted()
            }
    }
    fun refreshCustomers() {
        db.collection("customers")
            .get() // Troquei addSnapshotListener por get()
            .addOnSuccessListener { snapshot ->
                val names = snapshot.documents.mapNotNull { it.getString("name") }
                _availableCustomers.value = names.sorted()
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

    fun createNewOrder(customerName: String) {
        val newOrder = hashMapOf(
            "customerName" to customerName,
            "status" to "OPEN",
            "openedAt" to FieldValue.serverTimestamp(),
            "totalAmount" to 0.0
        )
        db.collection("orders").add(newOrder)
        db.collection("customers").document(customerName).set(mapOf("name" to customerName))
    }

    fun confirmOrderItems(orderId: String) {
        val itemsToSave = _tempItems.value
        if (itemsToSave.isEmpty()) return

        val batch = db.batch()
        val orderRef = db.collection("orders").document(orderId)
        var totalToAdd = 0.0

        itemsToSave.forEach { (productId, name, price) ->
            val newItemRef = orderRef.collection("items").document()
            batch.set(newItemRef, hashMapOf(
                "name" to name,
                "price" to price,
                "timestamp" to FieldValue.serverTimestamp()
            ))
            totalToAdd += price

            // Baixa de estoque
            val productRef = db.collection("products").document(productId)
            batch.update(productRef, "quantity", FieldValue.increment(-1.0))
        }

        batch.update(orderRef, "totalAmount", FieldValue.increment(totalToAdd))
        batch.commit().addOnSuccessListener { clearTempList() }
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
        val statsRef = db.collection("daily_stats").document(dateId)
        val orderRef = db.collection("orders").document(order.orderId)

        // Cálculo do lucro real da comanda
        val totalProfit = _confirmedItems.value.sumOf { item ->
            val product = inventoryProducts.value.find { it.description == item.first }
            val cost = product?.purchasePrice ?: 0.0
            item.second - cost
        }

        db.runTransaction { transaction ->
            val statsSnap = transaction.get(statsRef)
            // --- 0.5 PARA SALVAR OS ITENS NA COMANDA ---
            val summary = _confirmedItems.value.map {
                mapOf("name" to it.first, "price" to it.second)
            }
            transaction.update(orderRef, "itemsSummary", summary)
            // ----------------------------------------------------------

            // 1. Atualizar cada produto da comanda nas estatísticas (Valor Agregado)
            _confirmedItems.value.forEach { (name, price) ->
                val product = inventoryProducts.value.find { it.description == name }
                val productId = product?.code ?: name

                val topProductRef = statsRef.collection("top_products").document(productId)
                transaction.set(topProductRef,
                    mapOf(
                        "name" to name,
                        "quantity" to FieldValue.increment(1),
                        "totalRevenue" to FieldValue.increment(price) // Importante para o gráfico de pizza
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }

            // 2. Fechar a comanda
            transaction.update(orderRef, "status", "FINISHED", "closedAt", FieldValue.serverTimestamp())

            // 3. Atualizar Dashboard
            val saleTotal = order.totalAmount
            if (!statsSnap.exists()) {
                transaction.set(statsRef, hashMapOf(
                    "totalRevenue" to saleTotal,
                    "totalProfit" to totalProfit,
                    "count" to 1
                ))
            } else {
                transaction.update(statsRef,
                    "totalRevenue", FieldValue.increment(saleTotal),
                    "totalProfit", FieldValue.increment(totalProfit),
                    "count", FieldValue.increment(1)
                )
            }
            null
        }.addOnSuccessListener {
            onComplete(buildReceiptText(_confirmedItems.value, order.totalAmount, order.customerName, null))
        }
    }
    private fun observeSalesHistory() {
        val quickSalesFlow = MutableStateFlow<List<SaleModel>>(emptyList())


        db.collection("sales")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50) // limitando a 50 registros
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    SaleModel(
                        customerName = doc.getString("customerName") ?: "Venda Rápida",
                        total = doc.getDouble("total") ?: 0.0,
                        timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date(),
                        items = itemsRaw.map { SaleItem(it["name"] as? String ?: "", (it["price"] as? Number)?.toDouble() ?: 0.0) },
                        cpf = doc.getString("customerCpf") ?: ""
                    )
                } ?: emptyList()
                quickSalesFlow.value = list
            }

        val finishedOrdersFlow = MutableStateFlow<List<SaleModel>>(emptyList())


        db.collection("orders")
            .whereEqualTo("status", "FINISHED")
            .orderBy("closedAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val itemsRaw = doc.get("itemsSummary") as? List<Map<String, Any>> ?: emptyList()
                    SaleModel(
                        customerName = "Comanda: ${doc.getString("customerName") ?: "Mesa"}",
                        total = doc.getDouble("totalAmount") ?: 0.0,
                        timestamp = doc.getTimestamp("closedAt")?.toDate() ?: Date(),
                        items = itemsRaw.map { SaleItem(it["name"] as? String ?: "", (it["price"] as? Number)?.toDouble() ?: 0.0) },
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

        // Cálculo do lucro (Preço de Venda - Preço de Custo)
        val totalProfit = items.sumOf { item ->
            val product = inventoryProducts.value.find { it.code == item.first }
            val cost = product?.purchasePrice ?: 0.0
            item.third - cost
        }

        val dateId = java.text.SimpleDateFormat("yyyy_MM_dd", java.util.Locale.getDefault()).format(java.util.Date())
        val statsRef = db.collection("daily_stats").document(dateId)

        db.runTransaction { transaction ->
            val statsSnap = transaction.get(statsRef)

            // IMPORTANTE: Atualizar o Ranking de Produtos (Gráfico de Pizza)
            items.forEach { (_, productName, productPrice) ->
                // Usamos o NOME do produto como ID para bater com o Dashboard
                val topProductRef = statsRef.collection("top_products").document(productName)

                transaction.set(topProductRef, hashMapOf(
                    "name" to productName,
                    "quantity" to FieldValue.increment(1),
                    "totalRevenue" to FieldValue.increment(productPrice)
                ), com.google.firebase.firestore.SetOptions.merge())
            }

            // Salva a venda no histórico global
            val saleRef = db.collection("sales").document()
            val saleData = hashMapOf(
                "items" to items.map { mapOf("name" to it.second, "price" to it.third) },
                "total" to totalRevenue,
                "profit" to totalProfit,
                "customerName" to quickSaleCustomerName,
                "customerCpf" to quickSaleCustomerCpf,
                "timestamp" to FieldValue.serverTimestamp(),
                "type" to "QUICK_SALE"
            )
            transaction.set(saleRef, saleData)

            // Atualiza os totais do dia
            if (!statsSnap.exists()) {
                transaction.set(statsRef, hashMapOf(
                    "totalRevenue" to totalRevenue,
                    "totalProfit" to totalProfit,
                    "count" to 1
                ))
            } else {
                transaction.update(statsRef,
                    "totalRevenue", FieldValue.increment(totalRevenue),
                    "totalProfit", FieldValue.increment(totalProfit),
                    "count", FieldValue.increment(1)
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