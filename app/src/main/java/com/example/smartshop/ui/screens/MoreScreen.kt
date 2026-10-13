package com.example.smartshop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.Reminder
import com.example.smartshop.data.model.ShopUser
import com.example.smartshop.data.model.StringsProvider
import com.example.ui.theme.*

@Composable
fun MoreScreen(
    currentLanguage: AppLanguage,
    userSession: ShopUser,
    reminders: List<Reminder>,
    onSelectLanguage: (AppLanguage) -> Unit,
    onAddReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onLogout: () -> Unit
) {
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ShopBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Text(
                    text = StringsProvider.get("more", currentLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopNavyDark
                )
            }

            // Shop Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ShopNavy,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = ShopAmberLight, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = userSession.shopName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ShopNavyDark)
                            Text(text = "Owner: ${userSession.ownerName}", fontSize = 13.sp, color = ShopTextSecondary)
                            Text(text = "Phone: ${userSession.phone}", fontSize = 12.sp, color = ShopTextMuted)
                        }
                    }
                }
            }

            // Language Switcher Tile
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showLanguagePicker = true }
                        .testTag("more_language_tile"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ShopSurface),
                    border = BorderStroke(1.dp, ShopCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = ShopNavy)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Change App Language", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Current: ${currentLanguage.displayName} (${currentLanguage.subtitle})", fontSize = 12.sp, color = ShopTextSecondary)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ShopTextMuted)
                    }
                }
            }

            // Reminders Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shop Reminders (${reminders.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopNavyDark
                    )
                    Button(
                        onClick = { showAddReminderDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopNavy),
                        modifier = Modifier.testTag("add_reminder_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp)
                    }
                }
            }

            // Reminders Items
            if (reminders.isEmpty()) {
                item {
                    Text("No reminders right now.", fontSize = 13.sp, color = ShopTextMuted)
                }
            } else {
                items(reminders) { rem ->
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = rem.isCompleted,
                                    onCheckedChange = { onToggleReminder(rem) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = rem.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rem.isCompleted) ShopTextMuted else ShopTextPrimary
                                    )
                                    if (rem.details.isNotBlank()) {
                                        Text(text = rem.details, fontSize = 12.sp, color = ShopTextSecondary)
                                    }
                                }
                            }
                            IconButton(onClick = { onDeleteReminder(rem.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ShopTextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Supabase Architecture & Cloud Sync Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = ShopGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Supabase Postgres Backend & RLS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Shop ID: ${userSession.shopId} • Row Level Security active • Offline-First local database cache synchronized.",
                            fontSize = 12.sp,
                            color = Color(0xFF14532D)
                        )
                    }
                }
            }

            // Logout / Sign out
            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("more_logout_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopRed)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout / Switch Shop Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Language Picker Dialog
    if (showLanguagePicker) {
        Dialog(onDismissRequest = { showLanguagePicker = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Choose Language", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    AppLanguage.values().forEach { lang ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguagePicker = false
                                },
                            color = if (currentLanguage == lang) ShopNavy.copy(alpha = 0.1f) else ShopBackground
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${lang.displayName} (${lang.subtitle})", fontWeight = FontWeight.Bold)
                                if (currentLanguage == lang) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = ShopNavy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        var title by remember { mutableStateOf("") }
        var details by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddReminderDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShopSurface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "New Reminder", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ShopNavyDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Reminder (e.g. Call Ravi for payment)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showAddReminderDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onAddReminder(Reminder(title = title, details = details))
                                    showAddReminderDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ShopNavy)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
