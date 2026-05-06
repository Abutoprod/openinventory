package com.openinventory.app.data.datasource.local

import com.openinventory.app.data.database.dao.ProductDao
import com.openinventory.app.data.database.entity.ProductEntity

class LocalProductDataSource(private val productDao: ProductDao) {

    fun getProductsByStore(storeId: String) = productDao.getProductsByStore(storeId)
    fun getAllProducts() = productDao.getAllProducts()

    suspend fun clearProductsByStore(storeId: String) = productDao.clearProductsByStore(storeId)

    suspend fun saveProducts(products: List<ProductEntity>) = productDao.insertProducts(products)

    suspend fun updateQuantity(sku: String, qty: Int) = productDao.updateQuantity(sku, qty)

    suspend fun findByBarcode(barcode: String) = productDao.findByBarcode(barcode)

    suspend fun insert(product: ProductEntity) = productDao.insertProducts(listOf(product))
}