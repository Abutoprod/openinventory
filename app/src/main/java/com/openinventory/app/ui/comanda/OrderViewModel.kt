package com.openinventory.app.ui.comanda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.stateIn
import com.openinventory.app.data.datasource.local.ComandaFirebase
import com.openinventory.app.data.database.entity.OrderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.SharingStarted
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import com.google.firebase.firestore.firestore
import com.openinventory.app.data.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import com.openinventory.app.data.repository.ProductRepository
import com.openinventory.app.data.database.entity.ProductEntity
class OrderViewModel(private val repository: OrderRepository,private val productRepository: ProductRepository) : ViewModel() {

    private val db = Firebase.firestore
    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()
    private val _availableCustomers = MutableStateFlow<List<String>>(emptyList())
    val availableCustomers: StateFlow<List<String>> = _availableCustomers.asStateFlow()
    private val _tempItems = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val tempItems: StateFlow<List<Pair<String, Double>>> = _tempItems.asStateFlow()
    private val _confirmedItems = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val confirmedItems = _confirmedItems.asStateFlow()


    // Fluxo de produtos do estoque vindo do Room
    val inventoryProducts: StateFlow<List<ProductEntity>> = productRepository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Chamamos a escuta do Firebase logo que o ViewModel inicia
        observeFirebaseOrders()
        observeCustomers()
    }

    fun observeFirebaseOrders() {
        // Escuta a coleção "orders" do Firebase em tempo real
        db.collection("orders")
            .whereEqualTo("status", "OPEN")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Erro no Firebase: ${error.message}")
                    return@addSnapshotListener
                }

                val ordersList = snapshot?.documents?.mapNotNull { doc ->
                    // Converte o documento para sua OrderEntity
                    OrderEntity(
                        orderId = doc.id, // Usamos o ID real do documento do Firebase
                        customerName = doc.getString("customerName") ?: "Desconhecido",
                        isOpen = doc.getString("status") == "OPEN",
                        totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                        // O createdAt e lastUpdate você pode pegar do Firebase ou manter o default
                    )
                } ?: emptyList()

                _orders.value = ordersList
            }
    }

    fun createNewOrder(customerName: String) {
        val newOrder = hashMapOf(
            "customerName" to customerName,
            "status" to "OPEN",
            "openedAt" to FieldValue.serverTimestamp(),
            "total" to 0.0
        )

        db.collection("orders").add(newOrder)
        db.collection("customers").document(customerName).set(mapOf("name" to customerName))
    }
    private fun observeCustomers() {
        db.collection("customers")
            .addSnapshotListener { snapshot, _ ->
                val names = snapshot?.documents?.mapNotNull { it.getString("name") } ?: emptyList()
                _availableCustomers.value = names.sorted() // Deixa em ordem alfabética
            }



    }
    fun addItemToOrder(orderId: String, productName: String, price: Double) {
        val orderRef = db.collection("orders").document(orderId)
        val itemData = hashMapOf(
            "name" to productName,
            "price" to price,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // 1. Adiciona o produto na sub-coleção "items"
        orderRef.collection("items").add(itemData)
            .addOnSuccessListener {
                // 2. Atualiza o total da comanda usando increment (evita erro de cálculo)
                orderRef.update("totalAmount", FieldValue.increment(price))
            }
    }
    fun addToTempList(name: String, price: Double) {
        _tempItems.value = _tempItems.value + (name to price)
    }
    fun removeFromTempList(index: Int) {
        val newList = _tempItems.value.toMutableList()
        newList.removeAt(index)
        _tempItems.value = newList
    }

    fun clearTempList() {
        _tempItems.value = emptyList()
    }

    // Função para salvar tudo o que está no carrinho de uma vez
    fun confirmOrderItems(orderId: String) {
        val itemsToSave = _tempItems.value
        if (itemsToSave.isEmpty()) return

        val batch = db.batch()
        val orderRef = db.collection("orders").document(orderId)
        var totalToAdd = 0.0

        itemsToSave.forEach { (name, price) ->
            // 1. Cria o item dentro da comanda (para o extrato)
            val newItemRef = orderRef.collection("items").document()
            batch.set(newItemRef, hashMapOf(
                "name" to name,
                "price" to price,
                "timestamp" to FieldValue.serverTimestamp()
            ))
            totalToAdd += price

            // 2. Baixa de estoque no Firebase
            // IMPORTANTE: Aqui usamos o nome para achar o produto,
            // mas o ideal é usar o 'code' (SKU) se você tiver ele no Pair.
            val productRef = db.collection("products").document(name)
            batch.update(productRef, "quantity", FieldValue.increment(-1))
        }

        // 3. Atualiza o valor total da comanda
        batch.update(orderRef, "totalAmount", FieldValue.increment(totalToAdd))

        batch.commit().addOnSuccessListener {
            _tempItems.value = emptyList() // Limpa após o sucesso
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

}