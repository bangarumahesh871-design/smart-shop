package com.example.smartshop.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smartshop.ai.ParsedBillResult
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.DetectedBillItem
import com.example.smartshop.data.model.Product
import com.example.smartshop.data.model.StringsProvider
import com.example.ui.theme.*

@Composable
fun SupplierBillScannerScreen(
    currentLanguage: AppLanguage,
    scannedBill: ParsedBillResult?,
    isScanning: Boolean,
    existingProducts: List<Product>,
    onScanBill: (Bitmap?) -> Unit,
    onUpdateItem: (String, DetectedBillItem) -> Unit,
    onConfirmBill: () -> Unit,
    onClearBill: () -> Unit,
    onReadBillAloud: () -> Unit,
    onVoiceQuestion: (String) -> Unit
) {
    val context = LocalContext.current
    var itemToEdit by remember { mutableStateOf<DetectedBillItem?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    var permissionNotice by remember { mutableStateOf<String?>(null) }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onScanBill(bitmap)
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch()
            } catch (e: Exception) {
                permissionNotice = "Unable to open camera: ${e.message}. You can select an invoice from Gallery."
            }
        } else {
            permissionNotice = "Camera permission was denied. You can select an invoice from Gallery or load a sample bill."
        }
    }

    // Photo Gallery Launcher via zero-permission PickVisualMedia
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)
                onScanBill(bitmap)
            } catch (e: Exception) {
                permissionNotice = "Failed to load selected image: ${e.message}"
            }
        }
    }

    // Fallback Gallery Launcher
    val photoPickerFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)
                onScanBill(bitmap)
            } catch (e: Exception) {
                permissionNotice = "Failed to load selected image: ${e.message}"
            }
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = StringsProvider.get("scan_supplier_bill", currentLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Text(
                        text = "Scan invoice & update inventory stock",
                        fontSize = 12.sp,
                        color = ShopTextSecondary
                    )
                }

                if (scannedBill != null) {
                    TextButton(onClick = onClearBill) {
                        Text("Reset", color = ShopRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isScanning) {
                // Scanning Progress Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ShopNavy, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Extracting ALL Bill Items...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopNavyDark
                        )
                        Text(
                            text = "Multimodal OCR reading items, rates, taxes & quantities",
                            fontSize = 13.sp,
                            color = ShopTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (scannedBill == null) {
                // Capture / Scanner Launch Buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = ShopNavy,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Take Photo of Wholesale Bill",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopNavyDark
                        )
                        Text(
                            text = "Automatically detects all lines, items, prices, and matches with shop stock.",
                            fontSize = 13.sp,
                            color = ShopTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        try {
                                            cameraLauncher.launch()
                                        } catch (e: Exception) {
                                            permissionNotice = "Unable to open camera: ${e.message}. You can pick from Gallery."
                                        }
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scan_bill_camera_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ShopNavy),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera")
                            }

                            Button(
                                onClick = {
                                    try {
                                        pickVisualMediaLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } catch (e: Exception) {
                                        photoPickerFallbackLauncher.launch("image/*")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scan_bill_gallery_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAmber),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery")
                            }
                        }

                        if (permissionNotice != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = ShopRedContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = ShopRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = permissionNotice!!,
                                        color = Color(0xFF991B1B),
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { permissionNotice = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = ShopRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sample Wholesale Bill Demo Button
                        OutlinedButton(
                            onClick = { onScanBill(null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("scan_bill_demo_sample_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Sample Kirana Wholesale Invoice", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Scanned Invoice Results View
                // Voice Bill Reader (Bill Telling) Top Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = StringsProvider.get("bill_telling_title", currentLanguage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                            }

                            Button(
                                onClick = onReadBillAloud,
                                colors = ButtonDefaults.buttonColors(containerColor = ShopAmber),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("read_bill_aloud_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(StringsProvider.get("read_bill_aloud", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Voice Question Chips
                        val billQuestions = listOf(
                            "Bill total entha?",
                            "Enni items unnayi?",
                            "Supplier evaru?",
                            "Which item cost ekkuva?"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            billQuestions.take(3).forEach { q ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onVoiceQuestion(q) }
                                ) {
                                    Text(
                                        text = q,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Supplier & Bill Meta Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = scannedBill.supplierName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ShopNavyDark)
                            Text(text = scannedBill.billDate, fontSize = 13.sp, color = ShopTextSecondary)
                        }
                        Text(text = "Bill No: ${scannedBill.billNumber}", fontSize = 12.sp, color = ShopTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${StringsProvider.get("detected_items", currentLanguage)} (${scannedBill.items.size} Items)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopNavyDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Structured Editable Table
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(scannedBill.items) { index, item ->
                        val matchedProduct = existingProducts.find { it.id == item.matchedProductId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scanned_item_$index"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ShopSurface),
                            border = BorderStroke(
                                width = if (item.needsReview) 1.5.dp else 1.dp,
                                color = if (item.needsReview) ShopAmber else ShopCardBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.productName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ShopTextPrimary
                                            )
                                            if (item.needsReview) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = ShopAmberContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = StringsProvider.get("needs_review", currentLanguage),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF78350F),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Qty: ${item.quantity.toInt()} ${item.unit}  •  Rate: ₹${item.unitPrice.toInt()}",
                                            fontSize = 12.sp,
                                            color = ShopNavy
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Inventory Match Status
                                        if (matchedProduct != null) {
                                            Text(
                                                text = "✓ Matched: ${matchedProduct.name} (Cur Stock: ${matchedProduct.currentStock.toInt()})",
                                                fontSize = 11.sp,
                                                color = ShopGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Text(
                                                text = "✦ Will create as NEW PRODUCT",
                                                fontSize = 11.sp,
                                                color = Color(0xFF4F46E5),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${item.itemTotal.toInt()}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ShopNavyDark
                                        )

                                        IconButton(
                                            onClick = { itemToEdit = item },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = ShopNavy, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Calculated Grand Total & Confirm Stock update button
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopNavy.copy(alpha = 0.06f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bill Grand Total:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("₹${scannedBill.grandTotal.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = ShopNavy)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm & Add to Stock Button (Requires Confirmation!)
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_supplier_bill_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = StringsProvider.get("confirm_add_stock", currentLanguage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Edit Line Item Dialog
    if (itemToEdit != null) {
        val target = itemToEdit!!
        var eName by remember { mutableStateOf(target.productName) }
        var eQty by remember { mutableStateOf(target.quantity.toInt().toString()) }
        var eRate by remember { mutableStateOf(target.unitPrice.toInt().toString()) }
        var eUnit by remember { mutableStateOf(target.unit) }

        Dialog(onDismissRequest = { itemToEdit = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Edit Bill Item", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShopNavyDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = eName,
                        onValueChange = { eName = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = eQty,
                            onValueChange = { eQty = it },
                            label = { Text("Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = eRate,
                            onValueChange = { eRate = it },
                            label = { Text("Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = eUnit,
                            onValueChange = { eUnit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { itemToEdit = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val q = eQty.toDoubleOrNull() ?: target.quantity
                                val r = eRate.toDoubleOrNull() ?: target.unitPrice
                                val updated = target.copy(
                                    productName = eName,
                                    quantity = q,
                                    unitPrice = r,
                                    unit = eUnit,
                                    itemTotal = q * r,
                                    needsReview = false
                                )
                                onUpdateItem(target.id, updated)
                                itemToEdit = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopNavy)
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog before modifying inventory stock
    if (showConfirmDialog && scannedBill != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = ShopGreen) },
            title = { Text("Confirm & Update Stock?") },
            text = {
                Text(
                    "You are about to add ${scannedBill.items.size} items from '${scannedBill.supplierName}' (Total: ₹${scannedBill.grandTotal.toInt()}) directly into your inventory stock. Confirm?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onConfirmBill()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopGreen),
                    modifier = Modifier.testTag("dialog_bill_confirm_stock_update")
                ) {
                    Text("Confirm & Update")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
