package com.example.smartshop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: String = "shop_default",
    val name: String,
    val phone: String,
    val balance: Double = 0.0, // Positive: customer owes money (Udhar); Negative: customer gave advance
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class KhataType {
    CREDIT,   // Shopkeeper gave goods on credit -> increases balance owed
    PAYMENT   // Customer paid money (Cash/UPI) -> decreases balance owed
}

@Entity(tableName = "khata_transactions")
data class KhataTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: String = "shop_default",
    val customerId: Long,
    val type: KhataType,
    val amount: Double,
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)
