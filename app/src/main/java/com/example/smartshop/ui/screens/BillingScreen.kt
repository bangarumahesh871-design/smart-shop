package com.example.smartshop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smartshop.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    currentLanguage: AppLanguage,
    products: List<Product>,
    customers: List<Customer>,
    cartItems: List<CartItem>,
    selectedCustomer: Customer?,
    discount: Double,
    paymentMethod: PaymentMethod,
    lastCompletedSale: Sale?,
    showReceiptDialog: Boolean,
    onAddToCart: (Product) -> Unit,
    onUpdateCartQuantity: (Long, Double) -> Unit,
    onCustomerSelect: (Customer?) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onCompleteSale: () -> Unit,
    onDismissReceipt: () -> Unit,
    onClearCart: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val subtotal = remember(cartItems) { cartItems.sumOf { it.total } }
    val grandTotal = remember(subtotal, discount) { (subtotal - discount).coerceAtLeast(0.0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ShopBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Product Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("billing_product_search"),
                placeholder = { Text("Search products (బియ్యం, పప్పు, నూనె...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ShopNavy) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Area: Split List (Products to select & Cart)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Products Available List
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Products (${filteredProducts.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopNavyDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredProducts) { product ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onAddToCart(product) }
                                        .testTag("prod_item_${product.id}"),
                                    color = ShopNavy.copy(alpha = 0.04f),
                                    border = BorderStroke(1.dp, ShopCardBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = product.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ShopTextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "₹${product.sellingPrice.toInt()}/${product.unit} • Stock: ${product.currentStock.toInt()}",
                                                fontSize = 11.sp,
                                                color = if (product.isLowStock) ShopRed else ShopTextSecondary
                                            )
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = ShopNavy,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Current Bill / Cart View
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, ShopNavy.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Bill (${cartItems.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopNavyDark
                            )
                            if (cartItems.isNotEmpty()) {
                                TextButton(
                                    onClick = onClearCart,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Clear", color = ShopRed, fontSize = 12.sp)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        if (cartItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = ShopTextMuted,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cart is empty.\nTap a product to add.",
                                        fontSize = 12.sp,
                                        color = ShopTextMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(cartItems) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.productName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "₹${item.unitPrice.toInt()} × ${item.quantity.toInt()} = ₹${item.total.toInt()}",
                                                fontSize = 11.sp,
                                                color = ShopNavy
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onUpdateCartQuantity(item.productId, -1.0) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = ShopRed, modifier = Modifier.size(18.dp))
                                            }
                                            Text(
                                                text = "${item.quantity.toInt()}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            IconButton(
                                                onClick = { onUpdateCartQuantity(item.productId, 1.0) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = ShopGreen, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        // Customer Selection
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showCustomerPicker = true }
                                .testTag("billing_select_customer"),
                            color = if (selectedCustomer != null) ShopAmberContainer else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (selectedCustomer != null) ShopAmber else ShopTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedCustomer?.name ?: "Customer: Cash Walk-in",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ShopTextPrimary,
                                        maxLines = 1
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ShopTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Payment Method Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PaymentMethod.values().forEach { method ->
                                val isSelected = paymentMethod == method
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onPaymentMethodChange(method) }
                                        .testTag("pay_method_${method.name}"),
                                    color = if (isSelected) ShopNavy else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = method.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else ShopTextSecondary,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Totals Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ShopNavyDark)
                            Text("₹${grandTotal.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ShopNavy)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onCompleteSale,
                            enabled = cartItems.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("billing_complete_sale_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Sale (సేల్ రికార్డ్)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        Dialog(onDismissRequest = { showCustomerPicker = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Customer for Sale",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onCustomerSelect(null)
                                showCustomerPicker = false
                            },
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "Walk-in Cash Customer (No Khata)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp),
                            color = ShopNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(customers) { cust ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onCustomerSelect(cust)
                                        showCustomerPicker = false
                                    },
                                color = if (selectedCustomer?.id == cust.id) ShopAmberContainer else ShopBackground,
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
                                        Text(text = cust.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = cust.phone, fontSize = 12.sp, color = ShopTextSecondary)
                                    }
                                    Text(
                                        text = "Bal: ₹${cust.balance.toInt()}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cust.balance > 0) ShopRed else ShopGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Modal Dialog
    if (showReceiptDialog && lastCompletedSale != null) {
        Dialog(onDismissRequest = onDismissReceipt) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ShopGreenContainer,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ShopGreen, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Sale Completed!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShopNavyDark)
                    Text(text = "Sri Lakshmi Kirana Store", fontSize = 13.sp, color = ShopTextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bill No:", fontSize = 13.sp, color = ShopTextSecondary)
                        Text("#${lastCompletedSale.id}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Customer:", fontSize = 13.sp, color = ShopTextSecondary)
                        Text(lastCompletedSale.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment:", fontSize = 13.sp, color = ShopTextSecondary)
                        Text(lastCompletedSale.paymentMethod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ShopNavy)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Paid:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("₹${lastCompletedSale.finalAmount.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = ShopGreen)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismissReceipt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("receipt_done_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done (ముగింపు)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
