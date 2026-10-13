package com.example.smartshop.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartshop.data.model.Expense
import com.example.smartshop.data.model.PaymentMethod
import com.example.smartshop.data.model.Product
import com.example.smartshop.data.model.Reminder
import com.example.smartshop.data.model.Sale
import com.example.smartshop.data.model.SupplierBill
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    fun getProducts(shopId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE shopId = :shopId AND currentStock <= minStockLevel ORDER BY currentStock ASC")
    fun getLowStockProducts(shopId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE shopId = :shopId AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchProducts(shopId: String, query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET currentStock = currentStock + :quantityDelta, updatedAt = :time WHERE id = :productId")
    suspend fun adjustStock(productId: Long, quantityDelta: Double, time: Long = System.currentTimeMillis())

    @Query("UPDATE products SET sellingPrice = :newPrice, updatedAt = :time WHERE id = :productId")
    suspend fun updateProductPrice(productId: Long, newPrice: Double, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: Long)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getAllSales(shopId: String): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE shopId = :shopId AND createdAt >= :startTime ORDER BY createdAt DESC")
    fun getSalesSince(shopId: String, startTime: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE shopId = :shopId AND paymentMethod = :method AND createdAt >= :startTime")
    fun getSalesByMethodSince(shopId: String, method: PaymentMethod, startTime: Long): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE shopId = :shopId ORDER BY date DESC")
    fun getAllExpenses(shopId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE shopId = :shopId AND date >= :startTime ORDER BY date DESC")
    fun getExpensesSince(shopId: String, startTime: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)
}

@Dao
interface SupplierBillDao {
    @Query("SELECT * FROM supplier_bills WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getAllSupplierBills(shopId: String): Flow<List<SupplierBill>>

    @Query("SELECT * FROM supplier_bills WHERE id = :id LIMIT 1")
    suspend fun getSupplierBillById(id: Long): SupplierBill?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierBill(bill: SupplierBill): Long
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE shopId = :shopId ORDER BY dueDate ASC")
    fun getReminders(shopId: String): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}
