package com.openinventory.app.data.database.dao

import com.openinventory.app.data.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // Certifique-se que na sua classe ProductEntity o campo chama 'code'
    @Query("SELECT * FROM products WHERE code = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("DELETE FROM products")
    suspend fun clearTable()
}