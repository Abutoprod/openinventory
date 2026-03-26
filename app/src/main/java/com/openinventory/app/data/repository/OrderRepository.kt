package com.openinventory.app.data.repository
import com.openinventory.app.data.database.dao.OrderDao
import com.openinventory.app.data.database.entity.OrderEntity
import com.openinventory.app.data.database.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {

    // Retorna todas as comandas (O nosso SELECT * ORDER BY date DESC)
    fun getAllOrders(): Flow<List<OrderEntity>> {
        return orderDao.getAllOrders()
    }

    // Insere uma nova comanda (Cabeçalho)
    suspend fun insertOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
        // Aqui no futuro você pode chamar: firestore.collection("orders").add(order)
    }

    // Adiciona um item (Booster, Evento, etc) na comanda
    suspend fun addItemToOrder(item: OrderItemEntity) {
        orderDao.insertOrderItem(item)
    }

    // Fecha a comanda (Update Status)
    suspend fun closeOrder(orderId: String) {
        orderDao.closeOrder(orderId)
    }

    // Deleta uma comanda e seus itens (Cascateamento manual se necessário)
    suspend fun deleteOrder(order: OrderEntity) {
        orderDao.deleteOrder(order)
    }
}