package com.example.smartshop.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartshop.data.model.Customer
import com.example.smartshop.data.model.KhataTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY name ASC")
    fun getCustomers(shopId: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchCustomers(shopId: String, query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET balance = balance + :amountDelta WHERE id = :customerId")
    suspend fun updateCustomerBalance(customerId: Long, amountDelta: Double)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomer(customerId: Long)

    @Query("SELECT SUM(CASE WHEN balance > 0 THEN balance ELSE 0 END) FROM customers WHERE shopId = :shopId")
    fun getTotalPendingBalance(shopId: String): Flow<Double?>
}

@Dao
interface KhataTransactionDao {
    @Query("SELECT * FROM khata_transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<KhataTransaction>>

    @Query("SELECT * FROM khata_transactions WHERE shopId = :shopId ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(shopId: String, limit: Int = 20): Flow<List<KhataTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KhataTransaction): Long

    @Query("DELETE FROM khata_transactions WHERE customerId = :customerId")
    suspend fun deleteTransactionsForCustomer(customerId: Long)
}
