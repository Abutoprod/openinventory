package com.openinventory.app.data.datasource.local

import com.openinventory.app.data.database.dao.ProductDao
import com.openinventory.app.data.database.entity.ProductEntity

class LocalProductDataSource(private val productDao: ProductDao) {

    suspend fun saveProducts(products: List<ProductEntity>) = productDao.insertProducts(products)

    suspend fun findByBarcode(barcode: String): ProductEntity? = productDao.findByBarcode(barcode)

    suspend fun deleteAll() = productDao.clearTable()

    suspend fun insert(product: ProductEntity) {
        productDao.insert(product)     }

    // Novo: Adicione esse método no seu ProductDao para suportar o update de estoque
    suspend fun updateQuantity(sku: String, qty: Int) = productDao.updateQuantity(sku, qty)

    fun getAllProducts() = productDao.getAllProducts()
}