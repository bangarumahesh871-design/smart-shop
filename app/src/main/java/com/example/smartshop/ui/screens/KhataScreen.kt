package com.example.smartshop.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.smartshop.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun KhataScreen(
    currentLanguage: AppLanguage,
    customers: List<Customer>,
    selectedCustomerId: Long?,
    transactions: List<KhataTransaction>,
    onSelectCustomer: (Long?) -> Unit,
    onAddCustomer: (String, String, String) -> Unit,
    onRecordTransaction: (Long, KhataType, Double, String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddCreditDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    val selectedCustomer = remember(selectedCustomerId, customers) {
        customers.find { it.id == selectedCustomerId }
    }

    // BackHandler when inside Customer Detail view
    BackHandler(enabled = selectedCustomer != null) {
        onSelectCustomer(null)
    }

    val filteredCustomers = remember(searchQuery, customers) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery)
        }
    }

    val totalPending = remember(customers) {
        customers.filter { it.balance > 0 }.sumOf { it.balance }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ShopBackground
    ) {
        if (selectedCustomer == null) {
            // Customer List Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Total Udhar Summary Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ShopNavy),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = StringsProvider.get("pending_balance", currentLanguage),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "₹${totalPending.toInt()}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "${customers.count { it.balance > 0 }} customers have pending balance",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Button(
                            onClick = { showAddCustomerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_customer_fab")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search customer name or phone...") },
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
                        .testTag("khata_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Customer List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCustomers) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectCustomer(customer.id) }
                                .testTag("customer_card_${customer.id}"),
                            shape = RoundedCornerShape(16.dp),
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
                                        color = if (customer.balance > 0) ShopRedContainer else ShopGreenContainer,
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = customer.name.take(1).uppercase(Locale.ROOT),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (customer.balance > 0) ShopRed else ShopGreen
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ShopTextPrimary
                                        )
                                        Text(
                                            text = customer.phone,
                                            fontSize = 13.sp,
                                            color = ShopTextSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val bal = customer.balance
                                    if (bal > 0) {
                                        Text(
                                            text = "₹${bal.toInt()}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ShopRed
                                        )
                                        Text(
                                            text = StringsProvider.get("customer_has_to_pay", currentLanguage),
                                            fontSize = 11.sp,
                                            color = ShopRed.copy(alpha = 0.8f)
                                        )
                                    } else if (bal < 0) {
                                        Text(
                                            text = "₹${(-bal).toInt()}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ShopGreen
                                        )
                                        Text(
                                            text = StringsProvider.get("advance_balance", currentLanguage),
                                            fontSize = 11.sp,
                                            color = ShopGreen.copy(alpha = 0.8f)
                                        )
                                    } else {
                                        Text(
                                            text = "₹0",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ShopTextMuted
                                        )
                                        Text(
                                            text = "Settled",
                                            fontSize = 11.sp,
                                            color = ShopTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Customer Details & Transaction History View
            val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Navigation bar for detail view
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onSelectCustomer(null) },
                        modifier = Modifier.testTag("customer_detail_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    Text(
                        text = selectedCustomer.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )

                    Row {
                        // Call button
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedCustomer.phone}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = ShopGreen)
                        }
                        // WhatsApp reminder share
                        IconButton(onClick = {
                            val msg = "Namaste ${selectedCustomer.name}, your pending balance at Sri Lakshmi Kirana Store is ₹${selectedCustomer.balance.toInt()}. Please settle at your convenience."
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=91${selectedCustomer.phone}&text=${Uri.encode(msg)}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = ShopNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Balance Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedCustomer.balance > 0) ShopRedContainer else ShopGreenContainer
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedCustomer.balance > 0) StringsProvider.get("customer_has_to_pay", currentLanguage)
                            else StringsProvider.get("advance_balance", currentLanguage),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedCustomer.balance > 0) Color(0xFF991B1B) else Color(0xFF065F46)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${Math.abs(selectedCustomer.balance).toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selectedCustomer.balance > 0) ShopRed else ShopGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Large Dual Action Buttons (+ Udhar / - Jama)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showAddCreditDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("customer_give_credit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = StringsProvider.get("add_credit", currentLanguage),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { showRecordPaymentDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("customer_got_payment_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = StringsProvider.get("record_payment", currentLanguage),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Transaction History (లావాదేవీల రికార్డు)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopNavyDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                val custTransactions = transactions.filter { it.customerId == selectedCustomer.id }

                if (custTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No entries recorded yet for ${selectedCustomer.name}", color = ShopTextMuted, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(custTransactions) { tx ->
                            val isCredit = tx.type == KhataType.CREDIT
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isCredit) ShopRedContainer else ShopGreenContainer,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                    contentDescription = null,
                                                    tint = if (isCredit) ShopRed else ShopGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isCredit) "Gave Credit (అప్పు)" else "Got Payment (జమా)",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCredit) ShopRed else ShopGreen
                                            )
                                            Text(
                                                text = "${dateFormat.format(Date(tx.date))} • ${tx.notes}",
                                                fontSize = 12.sp,
                                                color = ShopTextSecondary
                                            )
                                        }
                                    }

                                    Text(
                                        text = (if (isCredit) "+" else "-") + "₹${tx.amount.toInt()}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isCredit) ShopRed else ShopGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newAddress by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddCustomerDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = StringsProvider.get("add_customer", currentLanguage),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(StringsProvider.get("customer_name", currentLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_customer_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text(StringsProvider.get("phone_number", currentLanguage)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_customer_phone_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Address / Village (ఊరు/చిరునామా)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddCustomerDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(StringsProvider.get("cancel", currentLanguage))
                        }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    onAddCustomer(newName, newPhone.ifBlank { "9876543210" }, newAddress)
                                    showAddCustomerDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_customer_save_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopNavy)
                        ) {
                            Text(StringsProvider.get("save", currentLanguage))
                        }
                    }
                }
            }
        }
    }

    // Add Credit Dialog (Financial Action)
    if (showAddCreditDialog && selectedCustomer != null) {
        var creditAmountText by remember { mutableStateOf("") }
        var creditNote by remember { mutableStateOf("Goods on credit") }

        Dialog(onDismissRequest = { showAddCreditDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Give Credit (అప్పు ఇవ్వండి)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopRed
                    )
                    Text(
                        text = "To: ${selectedCustomer.name}",
                        fontSize = 14.sp,
                        color = ShopTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = creditAmountText,
                        onValueChange = { creditAmountText = it },
                        label = { Text("Amount (రూపాయలు)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_credit_amount_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = creditNote,
                        onValueChange = { creditNote = it },
                        label = { Text("Note (సరుకుల వివరాలు)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddCreditDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val amt = creditAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onRecordTransaction(selectedCustomer.id, KhataType.CREDIT, amt, creditNote)
                                    showAddCreditDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_credit_confirm_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopRed)
                        ) {
                            Text("Add Credit")
                        }
                    }
                }
            }
        }
    }

    // Record Payment Dialog (Financial Action)
    if (showRecordPaymentDialog && selectedCustomer != null) {
        var paymentAmountText by remember { mutableStateOf(selectedCustomer.balance.coerceAtLeast(0.0).toInt().toString()) }
        var paymentNote by remember { mutableStateOf("Payment received") }

        Dialog(onDismissRequest = { showRecordPaymentDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Got Payment (జమా నమోదు)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopGreen
                    )
                    Text(
                        text = "From: ${selectedCustomer.name}",
                        fontSize = 14.sp,
                        color = ShopTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Amount Received (వచ్చిన మొత్తం)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_payment_amount_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Mode / Note (Cash/UPI/వివరాలు)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRecordPaymentDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val amt = paymentAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onRecordTransaction(selectedCustomer.id, KhataType.PAYMENT, amt, paymentNote)
                                    showRecordPaymentDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_payment_confirm_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopGreen)
                        ) {
                            Text("Save Payment")
                        }
                    }
                }
            }
        }
    }
}
