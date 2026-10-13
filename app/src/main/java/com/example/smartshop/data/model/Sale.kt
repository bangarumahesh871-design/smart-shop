package com.example.smartshop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod {
    CASH,
    UPI,
    CREDIT
}

data class CartItem(
    val productId: Long,
    val productName: String,
    val unit: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double = quantity * unitPrice
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: String = "shop_default",
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val totalAmount: Double,
    val discount: Double = 0.0,
    val finalAmount: Double = totalAmount - discount,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val itemsSummary: String = "", // JSON or readable summary
    val createdAt: Long = System.currentTimeMillis()
)
