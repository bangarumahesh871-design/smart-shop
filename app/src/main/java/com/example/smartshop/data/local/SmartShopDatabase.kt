package com.example.smartshop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartshop.data.model.Customer
import com.example.smartshop.data.model.Expense
import com.example.smartshop.data.model.KhataTransaction
import com.example.smartshop.data.model.Product
import com.example.smartshop.data.model.Reminder
import com.example.smartshop.data.model.Sale
import com.example.smartshop.data.model.SupplierBill

@Database(
    entities = [
        Customer::class,
        KhataTransaction::class,
        Product::class,
        Sale::class,
        Expense::class,
        SupplierBill::class,
        Reminder::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartShopDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun khataTransactionDao(): KhataTransactionDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun supplierBillDao(): SupplierBillDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: SmartShopDatabase? = null

        fun getDatabase(context: Context): SmartShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartShopDatabase::class.java,
                    "smart_shop_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
