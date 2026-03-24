package com.openinventory.app.data.database

import android.content.Context
import androidx.room.*

import com.openinventory.app.data.database.dao.ProductDao
import com.openinventory.app.data.database.entity.ProductEntity

// 1. Definimos quais entidades (tabelas) este banco possui
@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 2. Expomos o DAO para que o DataSource possa usá-lo
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 3. Padrão Singleton para garantir uma única instância do banco
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database" // Nome do arquivo .db no celular
                )
                    .fallbackToDestructiveMigration() // Se mudar a versão, ele limpa o banco (bom para dev)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}