package com.openinventory.app.data.repository

import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.openinventory.app.data.database.AppDatabase
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.datasource.local.FileDataSource
import com.openinventory.app.data.datasource.local.LocalProductDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import com.openinventory.app.data.database.dao.ProductDao
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductRepository(
    private val localProductDataSource: LocalProductDataSource,
    private val fileDataSource: FileDataSource,
    private val database: AppDatabase
) {
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // --- SINCRONIZAÇÃO FIREBASE ---
    suspend fun syncWithFirebase() {
        try {
            val snapshot = firestore.collection("products").get().await()
            val productsFromFirebase = snapshot.toObjects(ProductEntity::class.java)

            if (productsFromFirebase.isNotEmpty()) {
                refreshInventoryCatalog(productsFromFirebase)
                Log.d("SYNC_DEBUG", "Sincronizado com Firebase: ${productsFromFirebase.size} itens.")
            }
        } catch (e: Exception) {
            Log.e("SYNC_DEBUG", "Erro ao sincronizar: ${e.message}")
        }
    }

    suspend fun findByBarcode(barcode: String): ProductEntity? {
        // Chame através do data source que já está injetado no construtor
        return localProductDataSource.findByBarcode(barcode)
    }

    // --- ATUALIZAÇÃO DE ESTOQUE (BILATERAL) ---
    suspend fun updateProductQuantity(sku: String, newQuantity: Int) {
        // 1. Atualiza Local (Room)
        localProductDataSource.updateQuantity(sku, newQuantity)

        // 2. Atualiza Remoto (Firebase)
        // Usamos o barcode como ID do documento no Firestore
        firestore.collection("products").document(sku)
            .update("quantity", newQuantity)
            .addOnFailureListener { Log.e("FIREBASE_ERROR", "Falha ao subir atualização para $sku") }
    }

    // --- GERENCIAMENTO DE CATÁLOGO ---
    suspend fun refreshInventoryCatalog(products: List<ProductEntity>) {
        database.withTransaction {
            localProductDataSource.deleteAll()
            localProductDataSource.saveProducts(products)
        }
    }

    // --- IMPORTAÇÃO CSV ---
    // --- IMPORTAÇÃO CSV COM PUSH PARA FIREBASE ---
    suspend fun importCsv(uri: Uri): Int = withContext(Dispatchers.IO) {
            try {
                val productsFromFile = fileDataSource.parseCsv(uri)
                if (productsFromFile.isNotEmpty()) {
                    // 1. Salva no banco local (Room)
                    refreshInventoryCatalog(productsFromFile)

                    // 2. "PUSH" para o Firebase (O que estava faltando!)
                    uploadToFirebase(productsFromFile)

                    productsFromFile.size
                } else 0
            } catch (e: Exception) {
                Log.e("IMPORT_DEBUG", "Erro no CSV: ${e.message}")
                0
            }
        }

    // Função auxiliar para subir os dados
    private fun uploadToFirebase(products: List<ProductEntity>) {
        val batch = firestore.batch() // Usando Batch para ser eficiente (igual um Bulk Insert)

        products.forEach { product ->
            // Usamos o 'code' (código de barras) como ID do documento para evitar duplicidade
            val docRef = firestore.collection("products").document(product.code)
            batch.set(docRef, product)
        }

        batch.commit()
            .addOnSuccessListener { Log.d("FIREBASE_DEBUG", "Batch de ${products.size} itens enviado com sucesso!") }
            .addOnFailureListener { e -> Log.e("FIREBASE_DEBUG", "Erro no Batch: ${e.message}") }
    }
    suspend fun insertProduct(product: ProductEntity) {
        // 1. Salva no Room (Local)
        localProductDataSource.insert(product)

        // 2. Sobe para o Firebase (Remoto)
        val docRef = firestore.collection("products").document(product.code)
        docRef.set(product)
            .addOnSuccessListener { Log.d("FIREBASE", "Produto sincronizado!") }
            .addOnFailureListener { e -> Log.e("FIREBASE", "Erro ao subir produto", e) }
    }


    // --- LEITURA REATIVA ---
    fun getAllProducts(): Flow<List<ProductEntity>> = localProductDataSource.getAllProducts()
}