package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.StringsProvider
import com.example.smartshop.ui.components.ShopTopBar
import com.example.smartshop.ui.components.VoiceOverlayDialog
import com.example.smartshop.ui.screens.*
import com.example.smartshop.ui.viewmodel.ShopViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ShopAmber
import com.example.ui.theme.ShopNavy

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ShopViewModel = viewModel()
                SmartShopApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SmartShopApp(viewModel: ShopViewModel) {
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()

    // Database & business state flows
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val supplierBills by viewModel.supplierBills.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val pendingKhata by viewModel.totalPendingKhata.collectAsStateWithLifecycle()

    val todaySales by viewModel.todayTotalSales.collectAsStateWithLifecycle()
    val todayCashSales by viewModel.todayCashSales.collectAsStateWithLifecycle()
    val todayUpiSales by viewModel.todayUpiSales.collectAsStateWithLifecycle()
    val todayCreditSales by viewModel.todayCreditSales.collectAsStateWithLifecycle()
    val todayExpenses by viewModel.todayExpenses.collectAsStateWithLifecycle()

    // Cart / Billing state
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCustomerForSale by viewModel.selectedCustomerForSale.collectAsStateWithLifecycle()
    val billDiscount by viewModel.billDiscount.collectAsStateWithLifecycle()
    val billPaymentMethod by viewModel.billPaymentMethod.collectAsStateWithLifecycle()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsStateWithLifecycle()
    val showReceiptDialog by viewModel.showReceiptDialog.collectAsStateWithLifecycle()

    // Scanners state
    val scannedBill by viewModel.scannedBill.collectAsStateWithLifecycle()
    val isScanningBill by viewModel.isScanningBill.collectAsStateWithLifecycle()
    val scannedUpi by viewModel.scannedUpi.collectAsStateWithLifecycle()
    val isScanningUpi by viewModel.isScanningUpi.collectAsStateWithLifecycle()

    // Voice Assistant modal & state
    val isVoiceModalOpen by viewModel.isVoiceModalOpen.collectAsStateWithLifecycle()
    val voiceState by viewModel.voice.voiceState.collectAsStateWithLifecycle()
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val audioPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.openVoiceAssistant()
    }

    val launchVoiceWithPermission = {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.openVoiceAssistant()
        } else {
            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // Handle back button for secondary screens
    BackHandler(enabled = currentScreen != "home") {
        if (currentScreen == "khata" && selectedCustomerId != null) {
            viewModel.navigateTo("khata", null)
        } else {
            viewModel.navigateTo("home")
        }
    }

    if (isFirstLaunch) {
        LanguageSelectionScreen(
            currentLanguage = currentLanguage,
            onLanguageSelected = { chosenLang ->
                viewModel.selectLanguage(chosenLang)
            }
        )
    } else if (!userSession.isAuthenticated) {
        AuthScreen(
            currentLanguage = currentLanguage,
            onLoginSuccess = {
                viewModel.login("9876543210", "123456", true, {
                    viewModel.navigateTo("home")
                }, {})
            },
            onDirectBypass = {
                viewModel.login("shopkeeper@smartshop.in", "123456", false, {
                    viewModel.navigateTo("home")
                }, {})
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ShopTopBar(
                    currentLanguage = currentLanguage,
                    onLanguageClick = { viewModel.navigateTo("more") },
                    onSearchClick = { viewModel.navigateTo("search") },
                    onVoiceClick = { launchVoiceWithPermission() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    val navItems = listOf(
                        Triple("home", StringsProvider.get("home", currentLanguage), Icons.Default.Home),
                        Triple("sales", StringsProvider.get("sales", currentLanguage), Icons.Default.PointOfSale),
                        Triple("khata", StringsProvider.get("khata", currentLanguage), Icons.AutoMirrored.Filled.MenuBook),
                        Triple("stock", StringsProvider.get("stock", currentLanguage), Icons.Default.Inventory2),
                        Triple("reports", StringsProvider.get("reports", currentLanguage), Icons.Default.BarChart),
                        Triple("more", StringsProvider.get("more", currentLanguage), Icons.Default.MoreHoriz)
                    )

                    navItems.forEach { (id, label, icon) ->
                        val isSelected = currentScreen == id || (id == "khata" && currentScreen == "customer_detail")
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (id == "khata") viewModel.navigateTo("khata", null)
                                else viewModel.navigateTo(id)
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ShopNavy,
                                selectedTextColor = ShopNavy,
                                indicatorColor = ShopNavy.copy(alpha = 0.12f),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_item_$id")
                        )
                    }
                }
            },
            floatingActionButton = {
                // Prominent Voice Assistant Floating Microphone Button
                FloatingActionButton(
                    onClick = { launchVoiceWithPermission() },
                    containerColor = ShopAmber,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("main_voice_assistant_fab"),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "home" -> {
                        HomeScreen(
                            currentLanguage = currentLanguage,
                            todaySales = todaySales,
                            cashSales = todayCashSales,
                            upiSales = todayUpiSales,
                            creditSales = todayCreditSales,
                            todayExpenses = todayExpenses,
                            pendingKhata = pendingKhata,
                            lowStockItems = lowStockProducts,
                            recentSales = sales,
                            onQuickAction = { actionId ->
                                when (actionId) {
                                    "new_sale" -> viewModel.navigateTo("sales")
                                    "add_customer" -> viewModel.navigateTo("khata")
                                    "add_khata" -> viewModel.navigateTo("khata")
                                    "add_product" -> viewModel.navigateTo("stock")
                                    "scan_bill" -> viewModel.navigateTo("supplier_scanner")
                                    "scan_upi" -> viewModel.navigateTo("upi_scanner")
                                    "add_expense" -> viewModel.navigateTo("expenses")
                                    "voice_assistant" -> launchVoiceWithPermission()
                                }
                            },
                            onVoiceClick = { launchVoiceWithPermission() }
                        )
                    }

                    "sales" -> {
                        BillingScreen(
                            currentLanguage = currentLanguage,
                            products = products,
                            customers = customers,
                            cartItems = cartItems,
                            selectedCustomer = selectedCustomerForSale,
                            discount = billDiscount,
                            paymentMethod = billPaymentMethod,
                            lastCompletedSale = lastCompletedSale,
                            showReceiptDialog = showReceiptDialog,
                            onAddToCart = { prod -> viewModel.addToCart(prod) },
                            onUpdateCartQuantity = { pId, delta -> viewModel.updateCartItemQuantity(pId, delta) },
                            onCustomerSelect = { cust -> viewModel.setCustomerForSale(cust) },
                            onDiscountChange = { disc -> viewModel.setBillDiscount(disc) },
                            onPaymentMethodChange = { m -> viewModel.setBillPaymentMethod(m) },
                            onCompleteSale = { viewModel.completeSale {} },
                            onDismissReceipt = { viewModel.dismissReceiptDialog() },
                            onClearCart = { viewModel.clearCart() }
                        )
                    }

                    "khata" -> {
                        KhataScreen(
                            currentLanguage = currentLanguage,
                            customers = customers,
                            selectedCustomerId = selectedCustomerId,
                            transactions = if (selectedCustomerId != null) {
                                val txs by viewModel.repository.getTransactionsForCustomer(selectedCustomerId!!).collectAsState(initial = emptyList())
                                txs
                            } else emptyList(),
                            onSelectCustomer = { cId -> viewModel.navigateTo("khata", cId) },
                            onAddCustomer = { name, phone, address ->
                                viewModel.addCustomer(name, phone, address)
                            },
                            onRecordTransaction = { cId, type, amount, notes ->
                                viewModel.recordKhataTransaction(cId, type, amount, notes)
                            }
                        )
                    }

                    "stock" -> {
                        StockScreen(
                            currentLanguage = currentLanguage,
                            products = products,
                            onAddProduct = { p -> viewModel.addProduct(p) },
                            onUpdateProduct = { p -> viewModel.updateProduct(p) },
                            onAdjustStock = { pId, delta -> viewModel.adjustStock(pId, delta) },
                            onDeleteProduct = { pId -> viewModel.deleteProduct(pId) }
                        )
                    }

                    "supplier_scanner" -> {
                        SupplierBillScannerScreen(
                            currentLanguage = currentLanguage,
                            scannedBill = scannedBill,
                            isScanning = isScanningBill,
                            existingProducts = products,
                            onScanBill = { bmp -> viewModel.scanSupplierBill(bmp) },
                            onUpdateItem = { id, item -> viewModel.updateScannedBillItem(id, item) },
                            onConfirmBill = {
                                viewModel.confirmScannedBill {
                                    viewModel.navigateTo("stock")
                                }
                            },
                            onClearBill = { viewModel.clearScannedBill() },
                            onReadBillAloud = {
                                scannedBill?.let { b ->
                                    val msg = "ఈ బిల్లులో ${b.items.size} సరుకులు ఉన్నాయి. మొత్తం ₹${b.grandTotal.toInt()}. సప్లయర్ ${b.supplierName}."
                                    viewModel.voice.speak(msg)
                                }
                            },
                            onVoiceQuestion = { q -> viewModel.executeVoiceQuery(q) }
                        )
                    }

                    "upi_scanner" -> {
                        UpiScannerScreen(
                            currentLanguage = currentLanguage,
                            scannedUpi = scannedUpi,
                            isScanning = isScanningUpi,
                            customers = customers,
                            onScanUpi = { bmp -> viewModel.scanUpiScreenshot(bmp) },
                            onRecordVerifiedPayment = { cId, name, amt, utr ->
                                viewModel.recordVerifiedUpiPayment(cId, name, amt, utr) {
                                    viewModel.navigateTo("home")
                                }
                            },
                            onClearUpi = { viewModel.clearScannedUpi() }
                        )
                    }

                    "expenses" -> {
                        ExpensesScreen(
                            currentLanguage = currentLanguage,
                            expenses = expenses,
                            todayTotal = todayExpenses,
                            onAddExpense = { exp -> viewModel.addExpense(exp) },
                            onDeleteExpense = { id -> viewModel.deleteExpense(id) }
                        )
                    }

                    "reports" -> {
                        ReportsScreen(
                            currentLanguage = currentLanguage,
                            sales = sales,
                            expenses = expenses,
                            products = products,
                            customers = customers
                        )
                    }

                    "search" -> {
                        SearchScreen(
                            currentLanguage = currentLanguage,
                            customers = customers,
                            products = products,
                            sales = sales,
                            bills = supplierBills,
                            onSelectCustomer = { cId -> viewModel.navigateTo("khata", cId) },
                            onNavigate = { scr -> viewModel.navigateTo(scr) }
                        )
                    }

                    "more" -> {
                        MoreScreen(
                            currentLanguage = currentLanguage,
                            userSession = userSession,
                            reminders = reminders,
                            onSelectLanguage = { l -> viewModel.selectLanguage(l) },
                            onAddReminder = { r -> viewModel.addReminder(r) },
                            onToggleReminder = { r -> viewModel.toggleReminder(r) },
                            onDeleteReminder = { id -> viewModel.deleteReminder(id) },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }

                // Voice Assistant Modal
                if (isVoiceModalOpen) {
                    VoiceOverlayDialog(
                        voiceState = voiceState,
                        currentLanguage = currentLanguage,
                        onDismiss = { viewModel.closeVoiceAssistant() },
                        onStartListening = { viewModel.voice.startListening() },
                        onStopListening = { viewModel.voice.stopListening() },
                        onQuerySelect = { q -> viewModel.executeVoiceQuery(q) },
                        onConfirmAction = { action -> viewModel.confirmPendingVoiceAction(action) },
                        onCancelAction = { viewModel.cancelPendingVoiceAction() }
                    )
                }
            }
        }
    }
}
