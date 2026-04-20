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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.openinventory.app.core.config.CompanyConstants
import com.openinventory.app.data.database.entity.OrderEntity
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.repository.OrderRepository
import com.openinventory.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import com.openinventory.app.ui.history.SaleItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import com.openinventory.app.ui.history.SaleModel

class OrderViewModel(
    private val repository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val db = Firebase.firestore

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
        refreshCustomers()
        //observeCustomers()
        observeSalesHistory()
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
        // 1. Criamos a lista de mapas para o Firebase entender
        val itemsSummary = _confirmedItems.value.map {
            mapOf("name" to it.first, "price" to it.second)
        }

        val receipt = buildReceiptText(
            items = _confirmedItems.value,
            total = order.totalAmount,
            name = order.customerName,
            cpf = null
        )

        // 2. No update, passamos o itemsSummary e a data de fechamento
        db.collection("orders").document(order.orderId)
            .update(
                "status", "FINISHED",
                "itemsSummary", itemsSummary,
                "closedAt", FieldValue.serverTimestamp()
            )
            .addOnSuccessListener { onComplete(receipt) }
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

        val total = items.sumOf { it.third }
        val itemsAsPairs = items.map { it.second to it.third }
        val receipt = buildReceiptText(itemsAsPairs, total, quickSaleCustomerName, quickSaleCustomerCpf)

        val batch = db.batch()
        val saleRef = db.collection("sales").document()

        items.forEach { (productId, _, _) ->
            val productRef = db.collection("products").document(productId)
            batch.update(productRef, "quantity", FieldValue.increment(-1.0))
        }

        val saleData = hashMapOf(
            "items" to items.map { mapOf("name" to it.second, "price" to it.third) },
            "total" to total,
            "customerName" to quickSaleCustomerName,
            "customerCpf" to quickSaleCustomerCpf,
            "timestamp" to FieldValue.serverTimestamp(),
            "type" to "QUICK_SALE"
        )

        batch.set(saleRef, saleData)
        batch.commit().addOnSuccessListener {
            clearTempList()
            quickSaleCustomerName = ""
            quickSaleCustomerCpf = ""
            onComplete(receipt)
        }
    }
}