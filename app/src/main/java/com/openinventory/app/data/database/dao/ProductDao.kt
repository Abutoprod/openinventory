package com.openinventory.app.data.database.dao

import com.openinventory.app.data.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    // Certifique-se que na sua classe ProductEntity o campo chama 'code'
    @Query("SELECT * FROM products WHERE code = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE storeId = :storeId")
    fun getProductsByStore(storeId: String): Flow<List<ProductEntity>>

    @Query("DELETE FROM products WHERE storeId = :storeId")
    suspend fun clearProductsByStore(storeId: String)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("DELETE FROM products")
    suspend fun clearTable()

    // --- NOVO: Para atualizar o estoque diretamente (Sync com Firebase) ---
    @Query("UPDATE products SET quantity = :qty WHERE code = :sku")
    suspend fun updateQuantity(sku: String, qty: Int)

    @Query("""
    UPDATE products 
    SET quantity = quantity - :sellQty 
    WHERE code IN (SELECT childSku FROM product_bundle WHERE parentSku = :barcode)
""")
    suspend fun decreaseBundleStock(barcode: String, sellQty: Int)
}