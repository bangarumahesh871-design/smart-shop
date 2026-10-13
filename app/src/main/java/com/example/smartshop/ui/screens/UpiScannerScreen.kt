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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.Customer
import com.example.smartshop.data.model.StringsProvider
import com.example.smartshop.data.model.UpiPaymentExtraction
import com.example.ui.theme.*

@Composable
fun UpiScannerScreen(
    currentLanguage: AppLanguage,
    scannedUpi: UpiPaymentExtraction?,
    isScanning: Boolean,
    customers: List<Customer>,
    onScanUpi: (Bitmap?) -> Unit,
    onRecordVerifiedPayment: (Long?, String, Double, String) -> Unit,
    onClearUpi: () -> Unit
) {
    val context = LocalContext.current
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    var permissionNotice by remember { mutableStateOf<String?>(null) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onScanUpi(bitmap)
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
                permissionNotice = "Unable to open camera: ${e.message}. You can select a screenshot from Gallery."
            }
        } else {
            permissionNotice = "Camera permission was denied. You can select a screenshot from Gallery or load a sample."
        }
    }

    // Gallery Launcher via zero-permission PickVisualMedia
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)
                onScanUpi(bitmap)
            } catch (e: Exception) {
                permissionNotice = "Failed to load selected screenshot: ${e.message}"
            }
        }
    }

    // Fallback Gallery Launcher
    val galleryFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)
                onScanUpi(bitmap)
            } catch (e: Exception) {
                permissionNotice = "Failed to load selected screenshot: ${e.message}"
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
                        text = StringsProvider.get("scan_upi", currentLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Text(
                        text = "Verify PhonePe / Google Pay / Paytm screenshot",
                        fontSize = 12.sp,
                        color = ShopTextSecondary
                    )
                }

                if (scannedUpi != null) {
                    TextButton(onClick = onClearUpi) {
                        Text("Reset", color = ShopRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isScanning) {
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
                            text = "Analyzing UPI Screenshot...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopNavyDark
                        )
                        Text(
                            text = "Extracting payer, amount, reference number & timestamp",
                            fontSize = 13.sp,
                            color = ShopTextSecondary
                        )
                    }
                }
            } else if (scannedUpi == null) {
                // Upload / Scanner Card
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
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Scan Customer UPI Screenshot",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopNavyDark
                        )
                        Text(
                            text = "Never rely on screenshot without verification. This extracts details to help you check your bank SMS / UPI app.",
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
                                    .testTag("upi_camera_button"),
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
                                        galleryFallbackLauncher.launch("image/*")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("upi_gallery_button"),
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

                        OutlinedButton(
                            onClick = { onScanUpi(null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("upi_sample_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Sample PhonePe ₹500 Screenshot", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Scanned Details Verification Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.5.dp, Color(0xFF60A5FA))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = ShopNavyLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = StringsProvider.get("upi_verify_title", currentLanguage),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopNavyDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Amount Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Payment Amount", fontSize = 12.sp, color = ShopNavy)
                                Text(
                                    text = "₹${scannedUpi.amount.toInt()}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ShopNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        UpiDetailRow("Payer Name:", scannedUpi.payerName)
                        UpiDetailRow("UPI ID:", scannedUpi.upiId)
                        UpiDetailRow("Date & Time:", "${scannedUpi.date}, ${scannedUpi.time}")
                        UpiDetailRow("UTR / Ref No:", scannedUpi.utrRef)

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(14.dp))

                        // Destination selection (Customer Khata vs Counter Cash)
                        Text(
                            text = "Record payment to:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showCustomerPicker = true }
                                .testTag("upi_target_picker"),
                            color = if (selectedCustomer != null) ShopAmberContainer else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCustomer?.let { "${it.name} (Khata: ₹${it.balance.toInt()})" }
                                        ?: "Counter Sale (Direct Cashier UPI)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShopNavyDark
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Verification Notice
                        Text(
                            text = "⚠ Verify that money has actually entered your bank account before tapping confirm.",
                            fontSize = 12.sp,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = {
                                onRecordVerifiedPayment(
                                    selectedCustomer?.id,
                                    scannedUpi.payerName,
                                    scannedUpi.amount,
                                    scannedUpi.utrRef
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("upi_confirm_payment_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verify & Record ₹${scannedUpi.amount.toInt()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomerPicker) {
        Dialog(onDismissRequest = { showCustomerPicker = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.65f),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Select Customer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedCustomer = null
                                showCustomerPicker = false
                            },
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "Direct Counter / Cashier (No Customer)",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(customers) { c ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCustomer = c
                                        showCustomerPicker = false
                                    },
                                color = if (selectedCustomer?.id == c.id) ShopAmberContainer else ShopBackground
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = c.name, fontWeight = FontWeight.Bold)
                                    Text(text = "Udhar: ₹${c.balance.toInt()}", color = ShopRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpiDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = ShopTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ShopNavyDark)
    }
}
