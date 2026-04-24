package com.openinventory.app.ui.comanda
import com.openinventory.app.data.repository.ProductRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openinventory.app.data.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
class OrderViewModelFactory(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val db: FirebaseFirestore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            return OrderViewModel(orderRepository, productRepository, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}