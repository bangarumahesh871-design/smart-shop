package com.example.smartshop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.data.model.*
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    currentLanguage: AppLanguage,
    sales: List<Sale>,
    expenses: List<Expense>,
    products: List<Product>,
    customers: List<Customer>
) {
    var selectedTimeframe by remember { mutableStateOf("Today") } // "Today", "This Week", "This Month"

    val filteredSales = remember(selectedTimeframe, sales) {
        val now = System.currentTimeMillis()
        val duration = when (selectedTimeframe) {
            "Today" -> 86400000L
            "This Week" -> 86400000L * 7
            else -> 86400000L * 30
        }
        sales.filter { it.createdAt >= now - duration }
    }

    val filteredExpenses = remember(selectedTimeframe, expenses) {
        val now = System.currentTimeMillis()
        val duration = when (selectedTimeframe) {
            "Today" -> 86400000L
            "This Week" -> 86400000L * 7
            else -> 86400000L * 30
        }
        expenses.filter { it.date >= now - duration }
    }

    val totalSales = remember(filteredSales) { filteredSales.sumOf { it.finalAmount } }
    val cashSales = remember(filteredSales) { filteredSales.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.finalAmount } }
    val upiSales = remember(filteredSales) { filteredSales.filter { it.paymentMethod == PaymentMethod.UPI }.sumOf { it.finalAmount } }
    val creditSales = remember(filteredSales) { filteredSales.filter { it.paymentMethod == PaymentMethod.CREDIT }.sumOf { it.finalAmount } }
    val totalExpenses = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }
    val netCashInHand = remember(cashSales, upiSales, totalExpenses) { (cashSales + upiSales - totalExpenses) }
    val totalPendingKhata = remember(customers) { customers.filter { it.balance > 0 }.sumOf { it.balance } }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ShopBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = StringsProvider.get("reports", currentLanguage),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopNavyDark
                        )
                        Text(text = "Sales, cashflow & stock insights", fontSize = 12.sp, color = ShopTextSecondary)
                    }
                }
            }

            // Filter Tabs: Today / This Week / This Month
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ShopNavy.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    listOf("Today", "This Week", "This Month").forEach { period ->
                        val isSel = selectedTimeframe == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) ShopNavy else Color.Transparent)
                                .clickable { selectedTimeframe = period }
                                .padding(vertical = 8.dp)
                                .testTag("report_tab_$period"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else ShopNavyDark
                            )
                        }
                    }
                }
            }

            // Net Cash & Total Sales Hero
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ShopNavy),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Net Cash Received ($selectedTimeframe)", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        Text(
                            text = "₹${netCashInHand.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cash (₹${cashSales.toInt()}) + UPI (₹${upiSales.toInt()}) - Expenses (₹${totalExpenses.toInt()})",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Financial Summary Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReportMetricCard(
                            title = "Total Sales",
                            amount = totalSales,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            color = ShopNavy,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricCard(
                            title = "Expenses",
                            amount = totalExpenses,
                            icon = Icons.Default.MoneyOff,
                            color = ShopRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReportMetricCard(
                            title = "Cash Collected",
                            amount = cashSales,
                            icon = Icons.Default.Payments,
                            color = ShopGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricCard(
                            title = "UPI Received",
                            amount = upiSales,
                            icon = Icons.Default.QrCode,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReportMetricCard(
                            title = "Given on Credit",
                            amount = creditSales,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            color = ShopAmber,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricCard(
                            title = "Total Khata Udhar",
                            amount = totalPendingKhata,
                            icon = Icons.Default.AccountBalanceWallet,
                            color = ShopRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Top Products / Best Sellers
            item {
                Text(
                    text = "Inventory Highlights",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopNavyDark
                )
            }

            items(products.take(4)) { prod ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = prod.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${prod.category} • Rate: ₹${prod.sellingPrice.toInt()}/${prod.unit}", fontSize = 12.sp, color = ShopTextSecondary)
                        }
                        Text(
                            text = "${prod.currentStock.toInt()} ${prod.unit}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (prod.isLowStock) ShopRed else ShopGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ShopSurface),
        border = BorderStroke(1.dp, ShopCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 12.sp, color = ShopTextSecondary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "₹${amount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
