package com.openinventory.app.data.datasource.local
import com.openinventory.app.data.database.dao.ProductDao
import com.openinventory.app.data.database.entity.ProductEntity
class LocalProductDataSource(private val productDao: ProductDao) {
    suspend fun saveProducts(products: List<ProductEntity>) = productDao.insertProducts(products)
    suspend fun getProductByCode(code: String) = productDao.findByBarcode(code)
    suspend fun deleteAll() { productDao.clearTable() }
    suspend fun findByBarcode(barcode: String): ProductEntity? {
        return productDao.findByBarcode(barcode)
    }
    fun getAllProducts() = productDao.getAllProducts()
}