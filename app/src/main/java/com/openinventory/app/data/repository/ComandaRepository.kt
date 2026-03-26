package com.openinventory.app.data.repository
import com.google.firebase.Firebase
import  com.google.firebase.firestore.*
import com.openinventory.app.data.datasource.local.ComandaFirebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
class ComandaRepository {
    private val db = Firebase.firestore
    private val comandasRef = db.collection("comandas")

    // Abrir nova comanda
    fun abrirComanda(nome: String) {
        val novaComanda = ComandaFirebase(cliente = nome)
        comandasRef.add(novaComanda)
    }

    // Ouvir as comandas em tempo real (O "Pulo do Gato" para multi-dispositivo)
    fun getComandasAtivas(): Flow<List<ComandaFirebase>> = callbackFlow {
        val subscription = comandasRef
            .whereEqualTo("status", "ABERTA")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ComandaFirebase::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }
}