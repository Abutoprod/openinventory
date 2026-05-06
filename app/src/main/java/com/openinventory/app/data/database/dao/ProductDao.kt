package com.openinventory.app.data.database.dao

import com.openinventory.app.data.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // Adicione esta função para filtrar por loja
    @Query("SELECT * FROM products WHERE storeId = :storeId")
    fun getProductsByStore(storeId: String): Flow<List<ProductEntity>>

    // Adicione esta função para limpar apenas uma loja[cite: 7]
    @Query("DELETE FROM products WHERE storeId = :storeId")
    suspend fun clearProductsByStore(storeId: String)

    @Query("SELECT * FROM products WHERE code = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?
    @Query("SELECT * FROM products ORDER BY description ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    @Query("UPDATE products SET quantity = :qty WHERE code = :sku")
    suspend fun updateQuantity(sku: String, qty: Int)
}