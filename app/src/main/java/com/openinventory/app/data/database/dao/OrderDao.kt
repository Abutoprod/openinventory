package com.openinventory.app.data.database.dao
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow
import com.openinventory.app.data.database.entity.OrderEntity
import androidx.room.*
import com.openinventory.app.data.database.entity.OrderItemEntity
@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItemEntity)

    @Query("UPDATE orders SET isOpen = 0 WHERE orderId = :orderId")
    suspend fun closeOrder(orderId: String)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)
}