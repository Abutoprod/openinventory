package com.openinventory.app.data.database.dao
import com.openinventory.app.data.database.entity.Customer
import kotlinx.coroutines.flow.Flow
import androidx.room.*
@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)
}