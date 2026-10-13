package com.example.smartshop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.data.model.AppLanguage
import com.example.ui.theme.*

@Composable
fun LanguageSelectionScreen(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ShopBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = ShopNavy
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "SmartShop",
                        tint = ShopAmberLight,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome to SmartShop",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ShopNavyDark
            )
            Text(
                text = "దయచేసి మీ భాషను ఎంచుకోండి / अपनी भाषा चुनें",
                fontSize = 15.sp,
                color = ShopTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Language Cards
            AppLanguage.values().forEach { lang ->
                val isSelected = currentLanguage == lang
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onLanguageSelected(lang) }
                        .testTag("lang_select_${lang.code}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) ShopNavy.copy(alpha = 0.08f) else ShopSurface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) ShopNavy else ShopCardBorder
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = lang.displayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ShopNavy else ShopTextPrimary
                            )
                            Text(
                                text = lang.subtitle,
                                fontSize = 14.sp,
                                color = ShopTextSecondary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = ShopNavy,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onLanguageSelected(currentLanguage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("lang_continue_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ShopNavy),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.TELUGU -> "కొనసాగించండి (Continue)"
                        AppLanguage.HINDI -> "आगे बढ़ें (Continue)"
                        AppLanguage.ENGLISH -> "Continue"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
