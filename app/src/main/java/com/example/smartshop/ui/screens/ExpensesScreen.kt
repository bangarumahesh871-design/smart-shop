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
import com.example.smartshop.data.model.Expense
import com.example.smartshop.data.model.StringsProvider
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpensesScreen(
    currentLanguage: AppLanguage,
    expenses: List<Expense>,
    todayTotal: Double,
    onAddExpense: (Expense) -> Unit,
    onDeleteExpense: (Long) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    var showAddDialog by remember { mutableStateOf(false) }

    val totalMonth = remember(expenses) { expenses.sumOf { it.amount } }

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
                        text = "Shop Expenses (ఖర్చులు)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Text(
                        text = "Track rent, electricity, wages & daily shop costs",
                        fontSize = 12.sp,
                        color = ShopTextSecondary
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(StringsProvider.get("add_expense", currentLanguage))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Snapshot Cards (Today & Total)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ShopAmberContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = StringsProvider.get("today_expenses", currentLanguage), fontSize = 12.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "₹${todayTotal.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF92400E))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.dp, ShopCardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "This Month Total", fontSize = 12.sp, color = ShopTextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "₹${totalMonth.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = ShopNavyDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Expense History (${expenses.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ShopNavyDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses recorded yet.", color = ShopTextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(expenses) { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                        color = ShopRedContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = ShopRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(text = exp.category, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${dateFormat.format(Date(exp.date))} • ${exp.notes.ifBlank { "Daily shop expense" }}",
                                            fontSize = 12.sp,
                                            color = ShopTextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${exp.amount.toInt()}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ShopRed
                                    )
                                    IconButton(
                                        onClick = { onDeleteExpense(exp.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ShopTextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        val categories = listOf("Tea & Snacks", "Electricity", "Shop Rent", "Wages / Helpers", "Transport", "Miscellaneous")
        var selectedCat by remember { mutableStateOf(categories.first()) }
        var amountText by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Add Expense (ఖర్చు రాయండి)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShopRed)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Category (వర్గం)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSel = selectedCat == cat
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCat = cat },
                                color = if (isSel) ShopRed else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else ShopTextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (రూపాయలు)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_expense_amount_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Note / Reason (వివరాలు)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showAddDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onAddExpense(
                                        Expense(
                                            category = selectedCat,
                                            amount = amt,
                                            notes = notes
                                        )
                                    )
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_expense_save_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopRed)
                        ) {
                            Text("Save Expense")
                        }
                    }
                }
            }
        }
    }
}
