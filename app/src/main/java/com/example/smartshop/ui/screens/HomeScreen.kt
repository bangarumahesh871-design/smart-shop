package com.example.smartshop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    currentLanguage: AppLanguage,
    todaySales: Double,
    cashSales: Double,
    upiSales: Double,
    creditSales: Double,
    todayExpenses: Double,
    pendingKhata: Double,
    lowStockItems: List<Product>,
    recentSales: List<Sale>,
    onQuickAction: (String) -> Unit,
    onVoiceClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
    ) {
        // Today's Sales Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("home_hero_sales_card"),
                colors = CardDefaults.cardColors(containerColor = ShopNavy),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = StringsProvider.get("today_sales", currentLanguage),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Today (ఈ రోజు)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "₹${todaySales.toInt()}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Cash / UPI / Credit pill breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SaleTypeSubStat(
                            title = StringsProvider.get("cash", currentLanguage),
                            amount = cashSales,
                            icon = Icons.Default.Payments,
                            color = Color(0xFF34D399)
                        )
                        SaleTypeSubStat(
                            title = StringsProvider.get("upi", currentLanguage),
                            amount = upiSales,
                            icon = Icons.Default.QrCodeScanner,
                            color = Color(0xFF60A5FA)
                        )
                        SaleTypeSubStat(
                            title = StringsProvider.get("credit", currentLanguage),
                            amount = creditSales,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }
        }

        // Secondary Financial Snapshot: Khata Udhar & Expenses
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pending Khata (Udhar)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onQuickAction("khata") }
                        .testTag("home_pending_khata_card"),
                    colors = CardDefaults.cardColors(containerColor = ShopRedContainer),
                    border = BorderStroke(1.dp, ShopRed.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = ShopRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = StringsProvider.get("pending_balance", currentLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹${pendingKhata.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ShopRed
                        )
                        Text(
                            text = "Total Udhar",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B).copy(alpha = 0.7f)
                        )
                    }
                }

                // Today's Expenses
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onQuickAction("expenses") }
                        .testTag("home_today_expenses_card"),
                    colors = CardDefaults.cardColors(containerColor = ShopAmberContainer),
                    border = BorderStroke(1.dp, ShopAmber.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = ShopAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = StringsProvider.get("today_expenses", currentLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹${todayExpenses.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Shop Costs",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F).copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Low Stock Alert Banner (if any)
        if (lowStockItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onQuickAction("stock") }
                        .testTag("home_low_stock_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.5.dp, Color(0xFFF87171)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ShopRed,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${lowStockItems.size} ${StringsProvider.get("low_stock_alert", currentLanguage)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = lowStockItems.joinToString(", ") { "${it.name} (${it.currentStock.toInt()} ${it.unit})" },
                                fontSize = 12.sp,
                                color = ShopTextSecondary,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Stock",
                            tint = ShopRed
                        )
                    }
                }
            }
        }

        // Quick Action Section Title
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringsProvider.get("quick_actions", currentLanguage),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopNavyDark
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Quick Actions Grid (2 rows of 4 or 4 columns)
        item {
            val quickActions = listOf(
                QuickActionItem("new_sale", StringsProvider.get("new_sale", currentLanguage), Icons.Default.PointOfSale, ShopNavy, Color(0xFFDBEAFE)),
                QuickActionItem("add_customer", StringsProvider.get("add_customer", currentLanguage), Icons.Default.PersonAdd, ShopGreen, ShopGreenContainer),
                QuickActionItem("add_khata", StringsProvider.get("add_khata", currentLanguage), Icons.AutoMirrored.Filled.MenuBook, Color(0xFFB45309), ShopAmberContainer),
                QuickActionItem("add_product", StringsProvider.get("add_product", currentLanguage), Icons.Default.AddBox, Color(0xFF4F46E5), Color(0xFFEEF2FF)),
                QuickActionItem("scan_bill", StringsProvider.get("scan_supplier_bill", currentLanguage), Icons.Default.DocumentScanner, Color(0xFF0284C7), Color(0xFFE0F2FE)),
                QuickActionItem("scan_upi", StringsProvider.get("scan_upi", currentLanguage), Icons.Default.QrCodeScanner, Color(0xFF7C3AED), Color(0xFFF3E8FF)),
                QuickActionItem("add_expense", StringsProvider.get("add_expense", currentLanguage), Icons.Default.MoneyOff, ShopRed, ShopRedContainer),
                QuickActionItem("voice_assistant", StringsProvider.get("voice_assistant", currentLanguage), Icons.Default.Mic, ShopAmber, Color(0xFFFEF3C7))
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickActions.take(4).forEach { action ->
                        QuickActionButton(
                            item = action,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (action.id == "voice_assistant") onVoiceClick()
                                else onQuickAction(action.id)
                            }
                        )
                    }
                }
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickActions.drop(4).take(4).forEach { action ->
                        QuickActionButton(
                            item = action,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (action.id == "voice_assistant") onVoiceClick()
                                else onQuickAction(action.id)
                            }
                        )
                    }
                }
            }
        }

        // Recent Activity Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = StringsProvider.get("recent_transactions", currentLanguage),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ShopNavyDark
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (recentSales.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ShopSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = ShopTextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "No sales recorded yet today", color = ShopTextMuted, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(recentSales.take(5)) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = when (sale.paymentMethod) {
                                    PaymentMethod.CASH -> ShopGreenContainer
                                    PaymentMethod.UPI -> Color(0xFFDBEAFE)
                                    PaymentMethod.CREDIT -> ShopAmberContainer
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (sale.paymentMethod) {
                                            PaymentMethod.CASH -> Icons.Default.Payments
                                            PaymentMethod.UPI -> Icons.Default.QrCode
                                            PaymentMethod.CREDIT -> Icons.AutoMirrored.Filled.MenuBook
                                        },
                                        contentDescription = null,
                                        tint = when (sale.paymentMethod) {
                                            PaymentMethod.CASH -> ShopGreen
                                            PaymentMethod.UPI -> ShopNavyLight
                                            PaymentMethod.CREDIT -> ShopAmber
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = sale.customerName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopTextPrimary
                                )
                                Text(
                                    text = "${dateFormat.format(Date(sale.createdAt))} • ${sale.paymentMethod.name}",
                                    fontSize = 12.sp,
                                    color = ShopTextSecondary
                                )
                            }
                        }

                        Text(
                            text = "₹${sale.finalAmount.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ShopNavyDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleTypeSubStat(title: String, amount: Double, icon: ImageVector, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "₹${amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

data class QuickActionItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

@Composable
private fun QuickActionButton(
    item: QuickActionItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("action_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = ShopSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ShopCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = item.bgColor,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ShopTextPrimary,
                maxLines = 1
            )
        }
    }
}
