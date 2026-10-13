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
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.Product
import com.example.smartshop.data.model.StringsProvider
import com.example.ui.theme.*

@Composable
fun StockScreen(
    currentLanguage: AppLanguage,
    products: List<Product>,
    onAddProduct: (Product) -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onAdjustStock: (Long, Double) -> Unit,
    onDeleteProduct: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var stockInProduct by remember { mutableStateOf<Product?>(null) }

    val categories = remember(products) {
        listOf("All", "Low Stock") + products.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filteredProducts = remember(searchQuery, selectedCategory, products) {
        products.filter { p ->
            val matchSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.brand.contains(searchQuery, ignoreCase = true) ||
                    p.barcode.contains(searchQuery)

            val matchCategory = when (selectedCategory) {
                "All" -> true
                "Low Stock" -> p.isLowStock
                else -> p.category == selectedCategory
            }
            matchSearch && matchCategory
        }
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
            // Header with Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = StringsProvider.get("stock", currentLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Text(
                        text = "${products.size} Items • ${products.count { it.isLowStock }} Low Stock",
                        fontSize = 12.sp,
                        color = ShopTextSecondary
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("stock_add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(StringsProvider.get("add_product", currentLanguage))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search product name, brand or barcode...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ShopNavy) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stock_search_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val isLowStockPill = cat == "Low Stock"
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isSelected && isLowStockPill -> ShopRed
                            isSelected -> ShopNavy
                            isLowStockPill -> ShopRedContainer
                            else -> ShopSurface
                        },
                        border = BorderStroke(1.dp, if (isLowStockPill) ShopRed else ShopCardBorder)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> Color.White
                                isLowStockPill -> ShopRed
                                else -> ShopTextPrimary
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product Inventory List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_card_${product.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ShopSurface),
                        border = BorderStroke(
                            width = if (product.isLowStock) 1.5.dp else 1.dp,
                            color = if (product.isLowStock) ShopRed else ShopCardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = product.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ShopTextPrimary
                                        )
                                        if (product.isLowStock) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = ShopRedContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "LOW",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = ShopRed,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${product.brand.ifBlank { "General" }} • ${product.category}",
                                        fontSize = 12.sp,
                                        color = ShopTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Selling: ₹${product.sellingPrice.toInt()}/${product.unit}  (Cost: ₹${product.purchasePrice.toInt()})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ShopNavy
                                    )
                                }

                                // Current Stock Count
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${product.currentStock.toInt()} ${product.unit}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (product.isLowStock) ShopRed else ShopGreen
                                    )
                                    Text(
                                        text = "Min: ${product.minStockLevel.toInt()} ${product.unit}",
                                        fontSize = 11.sp,
                                        color = ShopTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons: Quick Stock In / Stock Out / Edit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Stock In (+)
                                    OutlinedButton(
                                        onClick = { stockInProduct = product },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopGreen)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(StringsProvider.get("stock_in", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Quick Stock Out (-1)
                                    OutlinedButton(
                                        onClick = { onAdjustStock(product.id, -1.0) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopRed)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(StringsProvider.get("stock_out", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { productToEdit = product },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ShopNavy, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { productToDelete = product },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ShopRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Stock In Dialog
    if (stockInProduct != null) {
        var addQtyText by remember { mutableStateOf("10") }
        Dialog(onDismissRequest = { stockInProduct = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Add Stock (సరుకు పెంచండి)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopGreen
                    )
                    Text(text = stockInProduct!!.name, fontSize = 14.sp, color = ShopTextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = addQtyText,
                        onValueChange = { addQtyText = it },
                        label = { Text("Quantity to add (${stockInProduct!!.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_in_quantity_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { stockInProduct = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val qty = addQtyText.toDoubleOrNull() ?: 0.0
                                if (qty > 0) {
                                    onAdjustStock(stockInProduct!!.id, qty)
                                    stockInProduct = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_in_confirm_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopGreen)
                        ) {
                            Text("+ Add Stock")
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    val isEditing = productToEdit != null
    if (showAddDialog || isEditing) {
        val target = productToEdit
        var name by remember { mutableStateOf(target?.name ?: "") }
        var brand by remember { mutableStateOf(target?.brand ?: "") }
        var category by remember { mutableStateOf(target?.category ?: "Groceries") }
        var unit by remember { mutableStateOf(target?.unit ?: "kg") }
        var purchasePrice by remember { mutableStateOf(target?.purchasePrice?.toInt()?.toString() ?: "") }
        var sellingPrice by remember { mutableStateOf(target?.sellingPrice?.toInt()?.toString() ?: "") }
        var currentStock by remember { mutableStateOf(target?.currentStock?.toInt()?.toString() ?: "") }
        var minStock by remember { mutableStateOf(target?.minStockLevel?.toInt()?.toString() ?: "5") }

        Dialog(onDismissRequest = {
            showAddDialog = false
            productToEdit = null
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isEditing) "Edit Product" else "Add New Product (సరుకు జోడించు)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name (సరుకు పేరు)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_prod_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (kg/pkt)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("Cost Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            label = { Text("Sell Price (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_prod_sell_price_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentStock,
                            onValueChange = { currentStock = it },
                            label = { Text("Current Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = minStock,
                            onValueChange = { minStock = it },
                            label = { Text("Min Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                showAddDialog = false
                                productToEdit = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val prod = Product(
                                        id = target?.id ?: 0,
                                        name = name,
                                        brand = brand,
                                        category = category,
                                        unit = unit,
                                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                        currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                                        minStockLevel = minStock.toDoubleOrNull() ?: 5.0
                                    )
                                    if (isEditing) onUpdateProduct(prod) else onAddProduct(prod)
                                    showAddDialog = false
                                    productToEdit = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_prod_save_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopNavy)
                        ) {
                            Text("Save Product")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to remove ${productToDelete!!.name} from inventory?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(productToDelete!!.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
