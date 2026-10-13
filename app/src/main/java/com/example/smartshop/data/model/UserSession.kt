package com.example.smartshop.data.model

data class ShopUser(
    val shopId: String = "shop_101",
    val shopName: String = "Sri Lakshmi Kirana & General Store",
    val ownerName: String = "Ramesh Kumar",
    val phone: String = "9876543210",
    val email: String = "shopkeeper@smartshop.in",
    val language: AppLanguage = AppLanguage.TELUGU,
    val isAuthenticated: Boolean = true,
    val isDemoMode: Boolean = false
)

data class UpiPaymentExtraction(
    val payerName: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val time: String = "",
    val upiId: String = "",
    val utrRef: String = "",
    val isConfident: Boolean = true
)
