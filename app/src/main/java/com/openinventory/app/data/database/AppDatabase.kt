package com.openinventory.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.openinventory.app.data.database.dao.ProductDao
import com.openinventory.app.data.database.entity.BundleEntity
import com.openinventory.app.data.database.entity.ProductEntity
import com.openinventory.app.data.database.dao.OrderDao
import com.openinventory.app.data.database.entity.OrderEntity
import com.openinventory.app.data.database.entity.OrderItemEntity

// 1. Centralizamos as entidades e subimos a versão para 2
@Database(
    entities = [ProductEntity::class, BundleEntity::class,OrderEntity::class,
        OrderItemEntity::class ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 2. Expomos o DAO
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 3. Singleton para garantir uma única instância
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database"
                )
                    .fallbackToDestructiveMigration() // Isso evita crashes ao mudar a versão em dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}