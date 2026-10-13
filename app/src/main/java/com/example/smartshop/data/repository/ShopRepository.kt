package com.example.smartshop.data.repository

import com.example.smartshop.data.local.SmartShopDatabase
import com.example.smartshop.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ShopRepository(private val db: SmartShopDatabase) {

    // --- Customers & Khata ---
    fun getCustomers(shopId: String): Flow<List<Customer>> = db.customerDao().getCustomers(shopId)

    suspend fun getCustomerById(id: Long): Customer? = db.customerDao().getCustomerById(id)

    fun searchCustomers(shopId: String, query: String): Flow<List<Customer>> =
        db.customerDao().searchCustomers(shopId, query)

    suspend fun addCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        db.customerDao().insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        db.customerDao().updateCustomer(customer)
    }

    suspend fun deleteCustomer(customerId: Long) = withContext(Dispatchers.IO) {
        db.customerDao().deleteCustomer(customerId)
        db.khataTransactionDao().deleteTransactionsForCustomer(customerId)
    }

    fun getTotalPendingBalance(shopId: String): Flow<Double?> =
        db.customerDao().getTotalPendingBalance(shopId)

    fun getTransactionsForCustomer(customerId: Long): Flow<List<KhataTransaction>> =
        db.khataTransactionDao().getTransactionsForCustomer(customerId)

    fun getRecentTransactions(shopId: String, limit: Int = 20): Flow<List<KhataTransaction>> =
        db.khataTransactionDao().getRecentTransactions(shopId, limit)

    suspend fun recordKhataTransaction(
        shopId: String,
        customerId: Long,
        type: KhataType,
        amount: Double,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val tx = KhataTransaction(
            shopId = shopId,
            customerId = customerId,
            type = type,
            amount = amount,
            notes = notes,
            date = System.currentTimeMillis()
        )
        val txId = db.khataTransactionDao().insertTransaction(tx)
        val balanceDelta = if (type == KhataType.CREDIT) amount else -amount
        db.customerDao().updateCustomerBalance(customerId, balanceDelta)
        txId
    }

    // --- Products & Inventory ---
    fun getProducts(shopId: String): Flow<List<Product>> = db.productDao().getProducts(shopId)

    fun getLowStockProducts(shopId: String): Flow<List<Product>> = db.productDao().getLowStockProducts(shopId)

    suspend fun getProductById(id: Long): Product? = db.productDao().getProductById(id)

    fun searchProducts(shopId: String, query: String): Flow<List<Product>> =
        db.productDao().searchProducts(shopId, query)

    suspend fun addProduct(product: Product): Long = withContext(Dispatchers.IO) {
        db.productDao().insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        db.productDao().updateProduct(product)
    }

    suspend fun updateProductPrice(productId: Long, newPrice: Double) = withContext(Dispatchers.IO) {
        db.productDao().updateProductPrice(productId, newPrice)
    }

    suspend fun adjustStock(productId: Long, quantityDelta: Double) = withContext(Dispatchers.IO) {
        db.productDao().adjustStock(productId, quantityDelta)
    }

    suspend fun deleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        db.productDao().deleteProduct(productId)
    }

    // --- Sales & Billing ---
    fun getAllSales(shopId: String): Flow<List<Sale>> = db.saleDao().getAllSales(shopId)

    fun getSalesSince(shopId: String, startTime: Long): Flow<List<Sale>> =
        db.saleDao().getSalesSince(shopId, startTime)

    suspend fun createSale(
        shopId: String,
        customerId: Long?,
        customerName: String,
        items: List<CartItem>,
        totalAmount: Double,
        discount: Double,
        paymentMethod: PaymentMethod
    ): Long = withContext(Dispatchers.IO) {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("productId", item.productId)
                put("productName", item.productName)
                put("unit", item.unit)
                put("quantity", item.quantity)
                put("unitPrice", item.unitPrice)
                put("total", item.total)
            }
            jsonArray.put(obj)
            // Deduct stock automatically
            db.productDao().adjustStock(item.productId, -item.quantity)
        }

        val finalAmount = (totalAmount - discount).coerceAtLeast(0.0)
        val sale = Sale(
            shopId = shopId,
            customerId = customerId,
            customerName = customerName,
            totalAmount = totalAmount,
            discount = discount,
            finalAmount = finalAmount,
            paymentMethod = paymentMethod,
            itemsSummary = jsonArray.toString(),
            createdAt = System.currentTimeMillis()
        )
        val saleId = db.saleDao().insertSale(sale)

        // If Credit sale, update Customer Khata
        if (paymentMethod == PaymentMethod.CREDIT && customerId != null) {
            val tx = KhataTransaction(
                shopId = shopId,
                customerId = customerId,
                type = KhataType.CREDIT,
                amount = finalAmount,
                notes = "Sale #$saleId (${items.size} items)",
                date = System.currentTimeMillis()
            )
            db.khataTransactionDao().insertTransaction(tx)
            db.customerDao().updateCustomerBalance(customerId, finalAmount)
        }

        saleId
    }

    // --- Supplier Bills ---
    fun getAllSupplierBills(shopId: String): Flow<List<SupplierBill>> =
        db.supplierBillDao().getAllSupplierBills(shopId)

    suspend fun confirmSupplierBill(
        shopId: String,
        supplierName: String,
        billNumber: String,
        billDate: String,
        totalAmount: Double,
        items: List<DetectedBillItem>
    ): Long = withContext(Dispatchers.IO) {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("name", item.productName)
                put("brand", item.brand)
                put("qty", item.quantity)
                put("unit", item.unit)
                put("price", item.unitPrice)
                put("total", item.itemTotal)
            }
            jsonArray.put(obj)

            // Update existing or add new product stock
            if (item.matchedProductId != null) {
                db.productDao().adjustStock(item.matchedProductId!!, item.quantity)
            } else {
                // Insert as new product
                val newProd = Product(
                    shopId = shopId,
                    name = item.productName,
                    brand = item.brand,
                    unit = item.unit,
                    purchasePrice = item.unitPrice,
                    sellingPrice = item.unitPrice * 1.15, // Suggested 15% margin
                    currentStock = item.quantity,
                    minStockLevel = 5.0
                )
                db.productDao().insertProduct(newProd)
            }
        }

        val bill = SupplierBill(
            shopId = shopId,
            supplierName = supplierName,
            billNumber = billNumber,
            billDate = billDate,
            totalAmount = totalAmount,
            itemCount = items.size,
            itemsJson = jsonArray.toString(),
            status = "CONFIRMED",
            createdAt = System.currentTimeMillis()
        )
        db.supplierBillDao().insertSupplierBill(bill)
    }

    // --- Expenses ---
    fun getAllExpenses(shopId: String): Flow<List<Expense>> = db.expenseDao().getAllExpenses(shopId)

    fun getExpensesSince(shopId: String, startTime: Long): Flow<List<Expense>> =
        db.expenseDao().getExpensesSince(shopId, startTime)

    suspend fun addExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        db.expenseDao().insertExpense(expense)
    }

    suspend fun deleteExpense(id: Long) = withContext(Dispatchers.IO) {
        db.expenseDao().deleteExpense(id)
    }

    // --- Reminders ---
    fun getReminders(shopId: String): Flow<List<Reminder>> = db.reminderDao().getReminders(shopId)

    suspend fun addReminder(reminder: Reminder): Long = withContext(Dispatchers.IO) {
        db.reminderDao().insertReminder(reminder)
    }

    suspend fun toggleReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        db.reminderDao().updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
    }

    suspend fun deleteReminder(id: Long) = withContext(Dispatchers.IO) {
        db.reminderDao().deleteReminder(id)
    }

    // --- Seed Realistic Initial Data ---
    suspend fun seedInitialDataIfEmpty(shopId: String) = withContext(Dispatchers.IO) {
        val existingProducts = db.productDao().getProducts(shopId).firstOrNull()
        if (existingProducts.isNullOrEmpty()) {
            val sampleProducts = listOf(
                Product(shopId = shopId, name = "Sona Masoori Rice (బియ్యం)", brand = "Lalitha", category = "Grains", unit = "kg", purchasePrice = 52.0, sellingPrice = 58.0, currentStock = 120.0, minStockLevel = 25.0),
                Product(shopId = shopId, name = "Toor Dal (కందిపప్పు)", brand = "Fortune", category = "Pulses", unit = "kg", purchasePrice = 145.0, sellingPrice = 160.0, currentStock = 35.0, minStockLevel = 10.0),
                Product(shopId = shopId, name = "Sunflower Oil (నూనె)", brand = "Freedom", category = "Oils", unit = "litre", purchasePrice = 120.0, sellingPrice = 135.0, currentStock = 4.0, minStockLevel = 15.0), // Low stock alert!
                Product(shopId = shopId, name = "Sugar (పంచదార)", brand = "Madhur", category = "Groceries", unit = "kg", purchasePrice = 40.0, sellingPrice = 46.0, currentStock = 85.0, minStockLevel = 20.0),
                Product(shopId = shopId, name = "Aashirvaad Atta (గోధుమ పిండి)", brand = "ITC", category = "Flour", unit = "packet", purchasePrice = 210.0, sellingPrice = 240.0, currentStock = 18.0, minStockLevel = 5.0),
                Product(shopId = shopId, name = "Tata Salt (ఉప్పు)", brand = "Tata", category = "Groceries", unit = "packet", purchasePrice = 22.0, sellingPrice = 26.0, currentStock = 3.0, minStockLevel = 10.0), // Low stock alert!
                Product(shopId = shopId, name = "Maggi 2-Minute Noodles", brand = "Nestle", category = "Snacks", unit = "packet", purchasePrice = 12.0, sellingPrice = 14.0, currentStock = 60.0, minStockLevel = 20.0),
                Product(shopId = shopId, name = "Red Label Tea (టీ పొడి)", brand = "Brooke Bond", category = "Beverages", unit = "packet", purchasePrice = 125.0, sellingPrice = 140.0, currentStock = 22.0, minStockLevel = 8.0)
            )
            sampleProducts.forEach { db.productDao().insertProduct(it) }

            val sampleCustomers = listOf(
                Customer(shopId = shopId, name = "Ravi Kumar (రవి)", phone = "9848022338", balance = 750.0, address = "Gandhi Nagar"),
                Customer(shopId = shopId, name = "Suresh Reddy (సురేష్)", phone = "9440112233", balance = 1200.0, address = "Main Bazaar"),
                Customer(shopId = shopId, name = "Lakshmi Devi (లక్ష్మి)", phone = "9988776655", balance = 0.0, address = "Temple Road"),
                Customer(shopId = shopId, name = "Venkat Rao (వెంకట్)", phone = "9123456789", balance = 320.0, address = "Station Road")
            )
            val customerIds = sampleCustomers.map { db.customerDao().insertCustomer(it) }

            // Add sample khata transactions
            db.khataTransactionDao().insertTransaction(
                KhataTransaction(shopId = shopId, customerId = customerIds[0], type = KhataType.CREDIT, amount = 1000.0, notes = "Rice & Dal purchase", date = System.currentTimeMillis() - 86400000L * 3)
            )
            db.khataTransactionDao().insertTransaction(
                KhataTransaction(shopId = shopId, customerId = customerIds[0], type = KhataType.PAYMENT, amount = 250.0, notes = "PhonePe UPI payment", date = System.currentTimeMillis() - 86400000L)
            )

            // Add sample today's sales
            val todayStart = System.currentTimeMillis() - 3600000L * 4
            db.saleDao().insertSale(
                Sale(shopId = shopId, customerName = "Walk-in Cash", totalAmount = 350.0, finalAmount = 350.0, paymentMethod = PaymentMethod.CASH, itemsSummary = "Atta, Tea", createdAt = todayStart)
            )
            db.saleDao().insertSale(
                Sale(shopId = shopId, customerName = "Walk-in UPI", totalAmount = 520.0, finalAmount = 520.0, paymentMethod = PaymentMethod.UPI, itemsSummary = "Sugar, Oil", createdAt = todayStart + 3600000L)
            )

            // Add sample expense
            db.expenseDao().insertExpense(
                Expense(shopId = shopId, category = "Tea & Snacks", amount = 60.0, notes = "Morning shop tea", date = System.currentTimeMillis() - 7200000L)
            )

            // Add sample reminder
            db.reminderDao().insertReminder(
                Reminder(shopId = shopId, title = "Collect balance from Suresh Reddy", details = "Call ₹1200 pending for 10 days", dueDate = System.currentTimeMillis() + 86400000L, type = "PAYMENT")
            )
            db.reminderDao().insertReminder(
                Reminder(shopId = shopId, title = "Order Freedom Oil stock", details = "Only 4 litres left in stock", dueDate = System.currentTimeMillis() + 86400000L * 2, type = "STOCK")
            )
        }
    }
}
