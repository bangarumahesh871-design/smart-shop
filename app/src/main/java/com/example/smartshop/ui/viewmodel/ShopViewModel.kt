package com.example.smartshop.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.ai.GeminiService
import com.example.smartshop.ai.ParsedBillResult
import com.example.smartshop.data.local.SmartShopDatabase
import com.example.smartshop.data.model.*
import com.example.smartshop.data.repository.ShopRepository
import com.example.smartshop.data.supabase.SupabaseManager
import com.example.smartshop.voice.PendingVoiceAction
import com.example.smartshop.voice.VoiceAssistantManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SmartShopDatabase.getDatabase(application)
    val repository = ShopRepository(db)
    val supabase = SupabaseManager(application)
    val gemini = GeminiService()
    val voice = VoiceAssistantManager(application)

    // Language & Onboarding
    private val prefs = application.getSharedPreferences("smartshop_app_prefs", 0)
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.values().find { it.code == prefs.getString("selected_lang", null) } ?: AppLanguage.TELUGU
    )
    val currentLanguage = _currentLanguage.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(prefs.getBoolean("is_first_launch", true))
    val isFirstLaunch = _isFirstLaunch.asStateFlow()

    // Navigation
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen = _currentScreen.asStateFlow()

    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId = _selectedCustomerId.asStateFlow()

    // Auth State
    private val _userSession = MutableStateFlow(
        ShopUser(
            shopId = supabase.currentShopId,
            language = _currentLanguage.value,
            isAuthenticated = prefs.getBoolean("is_logged_in", true) // Default logged in for smooth first touch
        )
    )
    val userSession = _userSession.asStateFlow()

    val currentShopId: String
        get() = _userSession.value.shopId

    // UI flows
    val customers: StateFlow<List<Customer>> = repository.getCustomers(currentShopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.getProducts(currentShopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.getLowStockProducts(currentShopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.getAllSales(currentShopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.getAllExpenses(currentShopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplierBills: StateFlow<List<SupplierBill>> = repository.getAllSupplierBills(currentShopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.getReminders(currentShopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPendingKhata: StateFlow<Double> = repository.getTotalPendingBalance(currentShopId)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Today's summary stats
    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    val todaySalesList = repository.getSalesSince(currentShopId, getTodayStartMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotalSales = todaySalesList.map { list -> list.sumOf { it.finalAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayCashSales = todaySalesList.map { list ->
        list.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.finalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayUpiSales = todaySalesList.map { list ->
        list.filter { it.paymentMethod == PaymentMethod.UPI }.sumOf { it.finalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayCreditSales = todaySalesList.map { list ->
        list.filter { it.paymentMethod == PaymentMethod.CREDIT }.sumOf { it.finalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayExpenses = repository.getExpensesSince(currentShopId, getTodayStartMillis())
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Cart / Billing state
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _selectedCustomerForSale = MutableStateFlow<Customer?>(null)
    val selectedCustomerForSale = _selectedCustomerForSale.asStateFlow()

    private val _billDiscount = MutableStateFlow(0.0)
    val billDiscount = _billDiscount.asStateFlow()

    private val _billPaymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val billPaymentMethod = _billPaymentMethod.asStateFlow()

    private val _lastCompletedSale = MutableStateFlow<Sale?>(null)
    val lastCompletedSale = _lastCompletedSale.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog = _showReceiptDialog.asStateFlow()

    // Supplier Bill Scanner state
    private val _scannedBill = MutableStateFlow<ParsedBillResult?>(null)
    val scannedBill = _scannedBill.asStateFlow()

    private val _isScanningBill = MutableStateFlow(false)
    val isScanningBill = _isScanningBill.asStateFlow()

    // UPI Scanner state
    private val _scannedUpi = MutableStateFlow<UpiPaymentExtraction?>(null)
    val scannedUpi = _scannedUpi.asStateFlow()

    private val _isScanningUpi = MutableStateFlow(false)
    val isScanningUpi = _isScanningUpi.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Voice Modal visibility
    private val _isVoiceModalOpen = MutableStateFlow(false)
    val isVoiceModalOpen = _isVoiceModalOpen.asStateFlow()

    init {
        voice.currentLanguage = _currentLanguage.value
        voice.onSpeechRecognized = { text ->
            executeVoiceQuery(text)
        }
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(currentShopId)
        }
    }

    fun selectLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        voice.currentLanguage = lang
        voice.setTtsLanguage(lang)
        prefs.edit().putString("selected_lang", lang.code).apply()
        prefs.edit().putBoolean("is_first_launch", false).apply()
        _isFirstLaunch.value = false
    }

    fun navigateTo(screen: String, customerId: Long? = null) {
        _selectedCustomerId.value = customerId
        _currentScreen.value = screen
    }

    fun openVoiceAssistant() {
        _isVoiceModalOpen.value = true
        voice.startListening()
    }

    fun closeVoiceAssistant() {
        _isVoiceModalOpen.value = false
        voice.stopListening()
        voice.stopSpeaking()
        voice.clearVoiceState()
    }

    fun executeVoiceQuery(query: String) {
        voice.processCommand(
            query = query,
            customers = customers.value,
            products = products.value,
            todaySales = todayTotalSales.value,
            todayExpenses = todayExpenses.value,
            activeBill = supplierBills.value.firstOrNull()
        )
    }

    fun confirmPendingVoiceAction(action: PendingVoiceAction) {
        viewModelScope.launch {
            when (action) {
                is PendingVoiceAction.AddKhataCredit -> {
                    repository.recordKhataTransaction(
                        shopId = currentShopId,
                        customerId = action.customerId,
                        type = KhataType.CREDIT,
                        amount = action.amount,
                        notes = "Voice command credit"
                    )
                    voice.speak("సరే! ${action.customerName} ఖాతాలో ₹${action.amount.toInt()} అప్పు జోడించబడింది.")
                }
                is PendingVoiceAction.RecordKhataPayment -> {
                    repository.recordKhataTransaction(
                        shopId = currentShopId,
                        customerId = action.customerId,
                        type = KhataType.PAYMENT,
                        amount = action.amount,
                        notes = "Voice command payment"
                    )
                    voice.speak("ధన్యవాదాలు! ${action.customerName} నుండి ₹${action.amount.toInt()} చెల్లింపు నమోదు చేయబడింది.")
                }
                is PendingVoiceAction.UpdateProductPrice -> {
                    repository.updateProductPrice(action.productId, action.newPrice)
                    voice.speak("${action.productName} ధర ₹${action.newPrice.toInt()} గా మార్చబడింది.")
                }
                is PendingVoiceAction.AdjustStock -> {
                    repository.adjustStock(action.productId, action.quantityDelta)
                    voice.speak("స్టాక్ సవరించబడింది.")
                }
                is PendingVoiceAction.NavigateTo -> {
                    navigateTo(action.route)
                }
            }
            voice.clearPendingAction()
        }
    }

    fun cancelPendingVoiceAction() {
        voice.clearPendingAction()
        voice.speak("రద్దు చేయబడింది.")
    }

    // --- Cart & Billing Methods ---
    fun addToCart(product: Product, quantity: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            val newQty = item.quantity + quantity
            current[existingIndex] = item.copy(quantity = newQty, total = newQty * item.unitPrice)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    productName = product.name,
                    unit = product.unit,
                    quantity = quantity,
                    unitPrice = product.sellingPrice
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: Long, delta: Double) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty, total = newQty * item.unitPrice)
            }
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomerForSale.value = null
        _billDiscount.value = 0.0
    }

    fun setCustomerForSale(customer: Customer?) {
        _selectedCustomerForSale.value = customer
    }

    fun setBillDiscount(discount: Double) {
        _billDiscount.value = discount.coerceAtLeast(0.0)
    }

    fun setBillPaymentMethod(method: PaymentMethod) {
        _billPaymentMethod.value = method
    }

    fun completeSale(onSuccess: (Sale) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val total = items.sumOf { it.total }
        val discount = _billDiscount.value
        val method = _billPaymentMethod.value
        val customer = _selectedCustomerForSale.value

        viewModelScope.launch {
            val saleId = repository.createSale(
                shopId = currentShopId,
                customerId = customer?.id,
                customerName = customer?.name ?: "Walk-in Cash Customer",
                items = items,
                totalAmount = total,
                discount = discount,
                paymentMethod = method
            )
            val sale = Sale(
                id = saleId,
                shopId = currentShopId,
                customerId = customer?.id,
                customerName = customer?.name ?: "Walk-in Cash Customer",
                totalAmount = total,
                discount = discount,
                finalAmount = (total - discount).coerceAtLeast(0.0),
                paymentMethod = method
            )
            _lastCompletedSale.value = sale
            _showReceiptDialog.value = true
            clearCart()
            onSuccess(sale)
        }
    }

    fun dismissReceiptDialog() {
        _showReceiptDialog.value = false
        _lastCompletedSale.value = null
    }

    // --- Supplier Bill Scanner Methods ---
    fun scanSupplierBill(bitmap: Bitmap?) {
        viewModelScope.launch {
            _isScanningBill.value = true
            val result = gemini.parseSupplierBill(bitmap)
            // Match with existing products
            val prods = products.value
            val matchedItems = result.items.map { item ->
                val matched = prods.find { p ->
                    p.name.lowercase().contains(item.productName.lowercase().take(5)) ||
                            item.productName.lowercase().contains(p.name.lowercase().take(5))
                }
                item.copy(matchedProductId = matched?.id)
            }
            _scannedBill.value = result.copy(items = matchedItems)
            _isScanningBill.value = false

            // Voice Bill Telling: announce bill summary aloud!
            val billSpeech = "ఈ బిల్లులో ${result.items.size} సరుకులు ఉన్నాయి. మొత్తం ₹${result.grandTotal.toInt()}. సప్లయర్ ${result.supplierName}."
            voice.speak(billSpeech)
        }
    }

    fun updateScannedBillItem(itemId: String, updatedItem: DetectedBillItem) {
        val current = _scannedBill.value ?: return
        val updatedList = current.items.map { if (it.id == itemId) updatedItem else it }
        val newTotal = updatedList.sumOf { it.itemTotal }
        _scannedBill.value = current.copy(items = updatedList, grandTotal = newTotal)
    }

    fun confirmScannedBill(onDone: () -> Unit) {
        val bill = _scannedBill.value ?: return
        viewModelScope.launch {
            repository.confirmSupplierBill(
                shopId = currentShopId,
                supplierName = bill.supplierName,
                billNumber = bill.billNumber,
                billDate = bill.billDate,
                totalAmount = bill.grandTotal,
                items = bill.items
            )
            _scannedBill.value = null
            voice.speak("సప్లయర్ బిల్లు విజయవంతంగా స్టాక్ లో చేర్చబడింది.")
            onDone()
        }
    }

    fun clearScannedBill() {
        _scannedBill.value = null
    }

    // --- UPI Scanner Methods ---
    fun scanUpiScreenshot(bitmap: Bitmap?) {
        viewModelScope.launch {
            _isScanningUpi.value = true
            val result = gemini.parseUpiScreenshot(bitmap)
            _scannedUpi.value = result
            _isScanningUpi.value = false
        }
    }

    fun recordVerifiedUpiPayment(customerId: Long?, customerName: String, amount: Double, utr: String, onDone: () -> Unit) {
        viewModelScope.launch {
            if (customerId != null) {
                repository.recordKhataTransaction(
                    shopId = currentShopId,
                    customerId = customerId,
                    type = KhataType.PAYMENT,
                    amount = amount,
                    notes = "UPI Payment Ref: $utr"
                )
            } else {
                // Direct Cashier / Counter UPI sale
                val sale = Sale(
                    shopId = currentShopId,
                    customerName = customerName.ifBlank { "UPI Counter Customer" },
                    totalAmount = amount,
                    finalAmount = amount,
                    paymentMethod = PaymentMethod.UPI,
                    itemsSummary = "Direct UPI payment UTR: $utr"
                )
                db.saleDao().insertSale(sale)
            }
            _scannedUpi.value = null
            voice.speak("రూపాయలు ${amount.toInt()} యూపీఐ చెల్లింపు విజయవంతంగా నమోదు చేయబడింది.")
            onDone()
        }
    }

    fun clearScannedUpi() {
        _scannedUpi.value = null
    }

    // Search
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Login / Logout
    fun login(phoneOrEmail: String, passOrOtp: String, isOtp: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (isOtp) {
                val res = supabase.verifyPhoneOtp(phoneOrEmail, passOrOtp)
                if (res.isSuccess) {
                    val uid = res.getOrNull() ?: "shop_101"
                    _userSession.value = _userSession.value.copy(shopId = uid, phone = phoneOrEmail, isAuthenticated = true)
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    onSuccess()
                } else {
                    onError("Invalid OTP. Try entering 123456 or resend.")
                }
            } else {
                val res = supabase.signInWithEmail(phoneOrEmail, passOrOtp)
                if (res.isSuccess) {
                    val uid = res.getOrNull() ?: "shop_101"
                    _userSession.value = _userSession.value.copy(shopId = uid, email = phoneOrEmail, isAuthenticated = true)
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    onSuccess()
                } else {
                    // Fallback to offline local sign in
                    _userSession.value = _userSession.value.copy(email = phoneOrEmail, isAuthenticated = true)
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    onSuccess()
                }
            }
        }
    }

    fun logout() {
        _userSession.value = _userSession.value.copy(isAuthenticated = false)
        prefs.edit().putBoolean("is_logged_in", false).apply()
        _currentScreen.value = "auth"
    }

    fun addCustomer(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.addCustomer(
                Customer(
                    shopId = currentShopId,
                    name = name,
                    phone = phone,
                    address = address
                )
            )
        }
    }

    fun recordKhataTransaction(customerId: Long, type: KhataType, amount: Double, notes: String) {
        viewModelScope.launch {
            repository.recordKhataTransaction(
                shopId = currentShopId,
                customerId = customerId,
                type = type,
                amount = amount,
                notes = notes
            )
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product.copy(shopId = currentShopId))
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun adjustStock(productId: Long, delta: Double) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta)
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense.copy(shopId = currentShopId))
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.addReminder(reminder.copy(shopId = currentShopId))
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.toggleReminder(reminder)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voice.onDestroy()
    }
}
