package com.example.smartshop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.data.model.AppLanguage
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    currentLanguage: AppLanguage,
    onLoginSuccess: () -> Unit,
    onDirectBypass: () -> Unit
) {
    var isPhoneMode by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("9876543210") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("shopkeeper@smartshop.in") }
    var password by remember { mutableStateOf("shopkeeper123") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendTimer by remember { mutableStateOf(30) }
    var canResend by remember { mutableStateOf(false) }

    // Countdown for OTP
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            resendTimer = 30
            canResend = false
            while (resendTimer > 0) {
                delay(1000)
                resendTimer--
            }
            canResend = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ShopBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(18.dp),
                color = ShopNavy
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LockPerson,
                        contentDescription = null,
                        tint = ShopAmberLight,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SmartShop Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ShopNavyDark
            )
            Text(
                text = "Secure access to your store Khata and stock",
                fontSize = 14.sp,
                color = ShopTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Auth Tab Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShopNavy.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPhoneMode) ShopNavy else Color.Transparent)
                        .clickable {
                            isPhoneMode = true
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_phone_otp"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Phone + OTP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPhoneMode) Color.White else ShopNavyDark
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isPhoneMode) ShopNavy else Color.Transparent)
                        .clickable {
                            isPhoneMode = false
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_email_pass"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Email + Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isPhoneMode) Color.White else ShopNavyDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isPhoneMode) {
                // Phone Number Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Mobile Number (ఫోన్ నంబర్)") },
                    leadingIcon = {
                        Text(
                            text = "+91 ",
                            fontWeight = FontWeight.Bold,
                            color = ShopNavy,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                AnimatedVisibility(visible = isOtpSent) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("Enter 6-Digit OTP (ఓటీపీ నంబర్)") },
                            placeholder = { Text("e.g. 123456") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = ShopNavy)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_otp_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (!canResend) "Resend in ${resendTimer}s" else "Didn't receive OTP?",
                                fontSize = 13.sp,
                                color = ShopTextMuted
                            )
                            TextButton(
                                onClick = {
                                    isOtpSent = true
                                    otpCode = "123456"
                                },
                                enabled = canResend,
                                modifier = Modifier.testTag("resend_otp_button")
                            ) {
                                Text(
                                    text = "Resend OTP",
                                    fontWeight = FontWeight.Bold,
                                    color = if (canResend) ShopNavy else Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!isOtpSent) {
                            if (phoneNumber.length < 10) {
                                errorMessage = "Please enter a valid 10-digit mobile number."
                            } else {
                                isOtpSent = true
                                errorMessage = null
                                otpCode = "123456" // Auto-fill demo OTP convenience
                            }
                        } else {
                            if (otpCode.length < 4) {
                                errorMessage = "Please enter valid OTP code."
                            } else {
                                isLoading = true
                                onLoginSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_phone_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (!isOtpSent) "Send OTP (ఓటీపీ పంపండి)" else "Verify & Login (ధృవీకరించండి)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Email + Password Mode
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ShopNavy)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ShopNavy)
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter both email and password."
                        } else {
                            isLoading = true
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_email_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Login to SmartShop",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Error display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = ShopRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fast Demo bypass
            TextButton(
                onClick = onDirectBypass,
                modifier = Modifier.testTag("auth_demo_bypass_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = ShopAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Instant Demo Mode (నేరుగా ప్రారంభించండి)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShopNavy
                    )
                }
            }
        }
    }
}
