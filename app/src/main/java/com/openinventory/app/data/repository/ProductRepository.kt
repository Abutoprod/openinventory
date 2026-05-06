package com.openinventory.app.data.repository

import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.openinventory.app.data.database.AppDatabase
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.datasource.local.FileDataSource
import com.openinventory.app.data.datasource.local.LocalProductDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductRepository(
    private val localProductDataSource: LocalProductDataSource,
    private val fileDataSource: FileDataSource,
    private val database: AppDatabase
) {
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // --- SINCRONIZAÇÃO FIREBASE FILTRADA POR LOJA ---
    suspend fun syncWithFirebase(storeId: String) {
        try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .await()

            val productsFromFirebase = snapshot.toObjects(ProductEntity::class.java)

            // Agora passamos o storeId para garantir que a limpeza local seja cirúrgica[cite: 8]
            refreshInventoryCatalog(productsFromFirebase, storeId)
            Log.d("SYNC_DEBUG", "Sincronizado: ${productsFromFirebase.size} itens da filial $storeId")

        } catch (e: Exception) {
            Log.e("SYNC_DEBUG", "Erro ao sincronizar filial $storeId: ${e.message}")
        }
    }

    suspend fun findByBarcode(barcode: String): ProductEntity? {
        return localProductDataSource.findByBarcode(barcode)
    }

    // --- ATUALIZAÇÃO DE ESTOQUE (BILATERAL) ---
    suspend fun updateProductQuantity(sku: String, newQuantity: Int, storeId: String) {
        // 1. Atualiza Local (Room)
        localProductDataSource.updateQuantity(sku, newQuantity)

        // 2. Atualiza Remoto (Firebase)
        // O ID do documento é composto para permitir o mesmo EAN em lojas diferentes com estoques diferentes
        val docId = "${storeId}_$sku"
        firestore.collection("products").document(docId)
            .update("quantity", newQuantity)
            .addOnFailureListener { Log.e("FIREBASE_ERROR", "Falha ao subir atualização para $sku na loja $storeId") }
    }

    // --- GERENCIAMENTO DE CATÁLOGO LOCAL ---
    suspend fun refreshInventoryCatalog(products: List<ProductEntity>, storeId: String) {
        database.withTransaction {
            // Agora o compilador encontrará esta função no DataSource
            localProductDataSource.clearProductsByStore(storeId)
            localProductDataSource.saveProducts(products)
        }
    }

    // --- IMPORTAÇÃO CSV COM VINCULO DE LOJA ---
    suspend fun importCsv(uri: Uri, storeId: String): Int = withContext(Dispatchers.IO) {
        try {
            val productsFromFile = fileDataSource.parseCsv(uri)
            if (productsFromFile.isNotEmpty()) {
                val productsWithStore = productsFromFile.map { it.copy(storeId = storeId) }

                // Em vez de limpar o catálogo, apenas salva os novos/atualizados
                refreshInventoryCatalog(productsWithStore, storeId)
                localProductDataSource.saveProducts(productsWithStore) // Salva no Room local

                // Push para o Firebase
                uploadToFirebase(productsWithStore, storeId)

                productsWithStore.size
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    // --- PUSH EM LOTE (BATCH) ---
    private fun uploadToFirebase(products: List<ProductEntity>, storeId: String) {
        val batch = firestore.batch()

        products.forEach { product ->
            // ID Único: loja + código (Ex: MATRIZ_789123)
            val docId = "${storeId}_${product.code}"
            val docRef = firestore.collection("products").document(docId)

            // Garante que o objeto tenha o storeId antes de subir
            val productToUpload = product.copy(storeId = storeId)
            batch.set(docRef, productToUpload, SetOptions.merge())
        }

        batch.commit()
            .addOnSuccessListener { Log.d("FIREBASE_DEBUG", "Batch enviado para $storeId com sucesso!") }
            .addOnFailureListener { e -> Log.e("FIREBASE_DEBUG", "Erro no Batch $storeId: ${e.message}") }
    }

    // --- INSERÇÃO INDIVIDUAL ---
    suspend fun insertProduct(product: ProductEntity, storeId: String) {
        val productWithStore = product.copy(storeId = storeId)

        // 1. Salva no Room (Local)
        localProductDataSource.insert(productWithStore)

        // 2. Sobe para o Firebase (Remoto)
        val docId = "${storeId}_${product.code}"
        val docRef = firestore.collection("products").document(docId)

        docRef.set(productWithStore, SetOptions.merge())
            .addOnSuccessListener { Log.d("FIREBASE", "Produto ${product.description} sincronizado em $storeId") }
            .addOnFailureListener { e -> Log.e("FIREBASE", "Erro ao subir produto", e) }
    }

    // --- LEITURA REATIVA ---
    // Agora ele só traz o que for da loja que você quer ver
    fun getProductsByStore(storeId: String): Flow<List<ProductEntity>> {
        return localProductDataSource.getProductsByStore(storeId)
    }
    fun getAllProducts(): Flow<List<ProductEntity>> {
        return localProductDataSource.getAllProducts()
    }

}