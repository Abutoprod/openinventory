package com.openinventory.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.openinventory.app.data.datasource.local.ComandaFirebase
import com.openinventory.app.data.datasource.local.ItemVenda
import kotlinx.coroutines.channels.awaitClose
// Importe aqui a sua classe Product do Room
// import com.openinventory.app.data.entities.Product

class TcgRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

    // 1. Abrir Comanda no Firebase
    fun abrirComanda(nome: String) {
        val nova = ComandaFirebase(cliente = nome)
        db.collection("comandas").add(nova)
            .addOnSuccessListener { println("Comanda aberta com sucesso!") }
            .addOnFailureListener { e -> println("Erro ao abrir comanda: $e") }
    }

    // 2. Adicionar Item e Dar Baixa no Estoque (Transaction)
    // Usamos 'Any' temporariamente para o Product se você ainda não importou a classe
    fun adicionarItem(comandaId: String, produtoNome: String, produtoId: String, preco: Double, qtd: Int) {
        val comandaRef = db.collection("comandas").document(comandaId)
        val produtoRef = db.collection("produtos").document(produtoId)

        db.runTransaction { transaction ->
            val snapshotProd = transaction.get(produtoRef)
            val estoqueAtual = snapshotProd.getLong("quantidade") ?: 0L

            if (estoqueAtual >= qtd) {
                // Atualiza Estoque
                transaction.update(produtoRef, "quantidade", estoqueAtual - qtd)

                // Aqui criaríamos o objeto ItemVenda para subir
                val novoItem = ItemVenda(
                    produtoId = produtoId,
                    nome = produtoNome,
                    quantidade = qtd,
                    precoUnitario = preco
                )

                // Em NoSQL, geralmente lemos a lista atual e mandamos de volta com o novo item
                val snapshotComanda = transaction.get(comandaRef)
                val comandaAtual = snapshotComanda.toObject(ComandaFirebase::class.java)
                val listaAtualizada = comandaAtual?.itens?.toMutableList() ?: mutableListOf()
                listaAtualizada.add(novoItem)

                val novoTotal = (comandaAtual?.total ?: 0.0) + (preco * qtd)

                transaction.update(comandaRef, "itens", listaAtualizada)
                transaction.update(comandaRef, "total", novoTotal)
            }
        }
    }
    // No TcgRepository.kt
    fun getComandasAtivas(): kotlinx.coroutines.flow.Flow<List<ComandaFirebase>> = kotlinx.coroutines.flow.callbackFlow {
        val subscription = db.collection("comandas")
            .whereEqualTo("status", "ABERTA")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.toObjects(ComandaFirebase::class.java) ?: emptyList()
                trySend(docs)
            }
        awaitClose { subscription.remove() }
    }
}