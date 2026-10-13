package com.example.smartshop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: String = "shop_default",
    val category: String, // Rent, Electricity, Tea/Snacks, Wages, Transport, Misc
    val amount: Double,
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: String = "shop_default",
    val title: String,
    val details: String = "",
    val dueDate: Long = System.currentTimeMillis() + 86400000L,
    val isCompleted: Boolean = false,
    val type: String = "PAYMENT" // PAYMENT, SUPPLIER, STOCK, GENERAL
)
