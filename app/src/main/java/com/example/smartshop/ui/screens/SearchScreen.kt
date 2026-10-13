package com.example.smartshop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.data.model.*
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    currentLanguage: AppLanguage,
    customers: List<Customer>,
    products: List<Product>,
    sales: List<Sale>,
    bills: List<SupplierBill>,
    onSelectCustomer: (Long) -> Unit,
    onNavigate: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val matchedCustomers = remember(query, customers) {
        if (query.isBlank()) emptyList()
        else customers.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }
    }

    val matchedProducts = remember(query, products) {
        if (query.isBlank()) emptyList()
        else products.filter { it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
    }

    val matchedBills = remember(query, bills) {
        if (query.isBlank()) emptyList()
        else bills.filter { it.supplierName.contains(query, ignoreCase = true) || it.billNumber.contains(query, ignoreCase = true) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ShopBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search customers, items, bills...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ShopNavy) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (query.isBlank()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ShopTextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Search anything in your shop", color = ShopTextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Customers Section
                    if (matchedCustomers.isNotEmpty()) {
                        item {
                            Text("Customers (${matchedCustomers.size})", fontWeight = FontWeight.Bold, color = ShopNavy, fontSize = 14.sp)
                        }
                        items(matchedCustomers) { c ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectCustomer(c.id)
                                        onNavigate("khata")
                                    },
                                colors = CardDefaults.cardColors(containerColor = ShopSurface),
                                border = BorderStroke(1.dp, ShopCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = c.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = c.phone, fontSize = 12.sp, color = ShopTextSecondary)
                                    }
                                    Text(
                                        text = "Bal: ₹${c.balance.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (c.balance > 0) ShopRed else ShopGreen
                                    )
                                }
                            }
                        }
                    }

                    // Products Section
                    if (matchedProducts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Products (${matchedProducts.size})", fontWeight = FontWeight.Bold, color = ShopNavy, fontSize = 14.sp)
                        }
                        items(matchedProducts) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onNavigate("stock") },
                                colors = CardDefaults.cardColors(containerColor = ShopSurface),
                                border = BorderStroke(1.dp, ShopCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Stock: ${p.currentStock.toInt()} ${p.unit}", fontSize = 12.sp, color = ShopTextSecondary)
                                    }
                                    Text(text = "₹${p.sellingPrice.toInt()}", fontWeight = FontWeight.Bold, color = ShopNavy)
                                }
                            }
                        }
                    }

                    // Bills Section
                    if (matchedBills.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Supplier Bills (${matchedBills.size})", fontWeight = FontWeight.Bold, color = ShopNavy, fontSize = 14.sp)
                        }
                        items(matchedBills) { b ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ShopSurface),
                                border = BorderStroke(1.dp, ShopCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = b.supplierName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Bill: ${b.billNumber} • ${b.billDate}", fontSize = 12.sp, color = ShopTextSecondary)
                                    }
                                    Text(text = "₹${b.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = ShopNavy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
