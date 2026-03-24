package com.openinventory.app.data.datasource.local

import android.content.Context
import android.net.Uri
import com.openinventory.app.data.database.entity.ProductEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import com.openinventory.app.data.repository.ProductRepository

class FileDataSource(private val context: Context) {

    // Função para ler o CSV e transformar em lista de objetos
    fun parseCsv(uri: Uri): List<ProductEntity> {
        val products = mutableListOf<ProductEntity>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = inputStream.bufferedReader()
                val lines = reader.readLines()

                // LOG: Ver quantas linhas o ficheiro tem no total
                android.util.Log.d("IMPORT_DEBUG", "Ficheiro lido. Total de linhas: ${lines.size}")

                lines.drop(1).forEachIndexed { index, line ->
                    // LOG: Ver o conteúdo bruto da linha
                    android.util.Log.d("IMPORT_DEBUG", "Linha $index: $line")

                    val tokens = line.split(",")
                    if (tokens.size >= 4) {
                        products.add(ProductEntity(code = tokens[0].trim(), description = tokens[1].trim(), price = tokens[2].trim().toDoubleOrNull()?:0.0, quantity = tokens[3].trim().toIntOrNull()?:0))
                    } else {
                        android.util.Log.w("IMPORT_DEBUG", "Linha $index inválida (poucas colunas): $line")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("IMPORT_DEBUG", "Erro fatal na leitura do ficheiro", e)
        }

        // LOG: Ver quantos produtos foram criados antes de enviar para o banco
        android.util.Log.d("IMPORT_DEBUG", "Total de produtos convertidos: ${products.size}")
        return products
    }

}