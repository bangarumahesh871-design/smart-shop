package com.example.smartshop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class DetectedBillItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var productName: String,
    var brand: String = "",
    var quantity: Double,
    var unit: String = "kg",
    var unitPrice: Double,
    var itemTotal: Double = quantity * unitPrice,
    var tax: Double = 0.0,
    var needsReview: Boolean = false,
    var matchedProductId: Long? = null,
    var isNewProduct: Boolean = false
)

@Entity(tableName = "supplier_bills")
data class SupplierBill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: String = "shop_default",
    val supplierName: String,
    val billNumber: String = "",
    val billDate: String = "",
    val totalAmount: Double,
    val tax: Double = 0.0,
    val discount: Double = 0.0,
    val itemCount: Int,
    val itemsJson: String = "",
    val status: String = "CONFIRMED", // PENDING_REVIEW, CONFIRMED
    val createdAt: Long = System.currentTimeMillis()
)
