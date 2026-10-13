package com.example.smartshop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: String = "shop_default",
    val name: String,
    val brand: String = "",
    val category: String = "General",
    val unit: String = "kg", // "kg", "packet", "litre", "pcs", "bag"
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStockLevel: Double = 5.0,
    val barcode: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentStock <= minStockLevel
}
