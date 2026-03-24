package com.openinventory.app.data.repository

import android.net.Uri
import android.util.Log
import com.openinventory.app.data.database.AppDatabase
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.datasource.local.FileDataSource
import com.openinventory.app.data.datasource.local.LocalProductDataSource
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProductRepository(
    private val localProductDataSource: LocalProductDataSource,
    private val fileDataSource: FileDataSource,
    private val database: AppDatabase // Adicionado para suportar Transaction
) {

    suspend fun getProductBySku(sku: String): ProductEntity? {
        return localProductDataSource.findByBarcode(sku)
    }

    // Esta função agora limpa TUDO antes de salvar
    suspend fun refreshInventoryCatalog(products: List<ProductEntity>) {
        // withTransaction garante que se a luz cair ou o app crashar no meio,
        // ele não deixa o banco vazio. Ou faz tudo ou nada.
        database.withTransaction {
            localProductDataSource.deleteAll() // Você precisará criar essa função no seu LocalDataSource/DAO
            localProductDataSource.saveProducts(products)
        }
    }

    suspend fun importCsv(uri: Uri): Int {
        return withContext(Dispatchers.IO) { // Sempre em IO para não travar a tela (ANR)
            Log.d("IMPORT_DEBUG", "Iniciando importação do URI: $uri")

            val productsFromFile = fileDataSource.parseCsv(uri)

            if (productsFromFile.isNotEmpty()) {
                Log.d("IMPORT_DEBUG", "Limpando banco e inserindo ${productsFromFile.size} produtos.")

                // Chama a função que limpa e salva
                refreshInventoryCatalog(productsFromFile)

                productsFromFile.size
            } else {
                Log.e("IMPORT_DEBUG", "CSV vazio ou erro na leitura. Nada alterado.")
                0
            }
        }
    }

    fun getAllProducts(): Flow<List<ProductEntity>> {
        return localProductDataSource.getAllProducts()
    }
}