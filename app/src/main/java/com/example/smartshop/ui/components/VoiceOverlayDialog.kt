package com.example.smartshop.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.animation.core.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.StringsProvider
import com.example.smartshop.voice.PendingVoiceAction
import com.example.smartshop.voice.VoiceState
import com.example.ui.theme.*

@Composable
fun VoiceOverlayDialog(
    voiceState: VoiceState,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onQuerySelect: (String) -> Unit,
    onConfirmAction: (PendingVoiceAction) -> Unit,
    onCancelAction: () -> Unit
) {
    val context = LocalContext.current
    var inputQueryText by remember { mutableStateOf("") }

    LaunchedEffect(voiceState.spokenText) {
        if (voiceState.spokenText.isNotBlank() &&
            voiceState.spokenText != "Listening (వినబడుతోంది)..." &&
            voiceState.spokenText != "..."
        ) {
            inputQueryText = voiceState.spokenText
        }
    }

    // System RecognizerIntent Launcher (Zero failure on all Android devices)
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputQueryText = spoken
                onQuerySelect(spoken)
            }
        }
    }

    val launchSystemSpeech = {
        val langTag = when (currentLanguage) {
            AppLanguage.TELUGU -> "te-IN"
            AppLanguage.HINDI -> "hi-IN"
            AppLanguage.ENGLISH -> "en-IN"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command (e.g. Rice stock entha?)")
        }
        try {
            speechIntentLauncher.launch(intent)
        } catch (_: Exception) {
            onStartListening()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState.isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val sampleQueries = listOf(
        "Ravi balance entha?",
        "Ravi khata lo 500 add cheyyi",
        "Ravi 200 pay chesadu",
        "Total khata entha?",
        "Rice stock entha undi?",
        "Add 25 kg rice",
        "Low stock entha undi?",
        "Rice rate 78 cheyyi",
        "Today sales entha?",
        "New sale create cheyyi",
        "Today expenses entha?",
        "Ee bill total entha?"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = ShopSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = null,
                            tint = ShopAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = StringsProvider.get("voice_assistant", currentLanguage),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopNavyDark
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("voice_dialog_close")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing Mic Button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .background(
                            if (voiceState.isListening) ShopAmberLight.copy(alpha = 0.25f)
                            else ShopNavy.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (voiceState.isListening) onStopListening() else onStartListening()
                        },
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                if (voiceState.isListening) ShopAmber else ShopNavy,
                                CircleShape
                            )
                            .testTag("voice_mic_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (voiceState.isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (voiceState.isListening) StringsProvider.get("voice_assistant_listening", currentLanguage)
                    else StringsProvider.get("mic_tap_to_speak", currentLanguage),
                    fontSize = 14.sp,
                    color = if (voiceState.isListening) ShopAmber else ShopTextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                // Editable voice transcript / text command input
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = inputQueryText,
                    onValueChange = { inputQueryText = it },
                    placeholder = { Text("Speak or type voice command...") },
                    leadingIcon = {
                        IconButton(onClick = launchSystemSpeech) {
                            Icon(Icons.Default.Mic, contentDescription = "System Speech Recognition", tint = ShopAmber)
                        }
                    },
                    trailingIcon = {
                        if (inputQueryText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onQuerySelect(inputQueryText)
                                },
                                modifier = Modifier.testTag("voice_send_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Submit", tint = ShopNavy)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_transcript_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Assistant reply
                if (voiceState.assistantReply.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ShopGreenContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = ShopGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = voiceState.assistantReply,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                // Financial / Destructive Pending Confirmation Card
                if (voiceState.pendingAction != null) {
                    val pending = voiceState.pendingAction
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ShopAmberContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ShopAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = StringsProvider.get("voice_confirmation_title", currentLanguage),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val actionPrompt = when (pending) {
                                is PendingVoiceAction.AddKhataCredit -> pending.confirmationPrompt
                                is PendingVoiceAction.RecordKhataPayment -> pending.confirmationPrompt
                                is PendingVoiceAction.UpdateProductPrice -> pending.confirmationPrompt
                                is PendingVoiceAction.AdjustStock -> pending.confirmationPrompt
                                is PendingVoiceAction.NavigateTo -> "Navigate to ${pending.route}?"
                            }
                            Text(
                                text = actionPrompt,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF451A03)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onCancelAction,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("voice_action_cancel_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopRed)
                                ) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = StringsProvider.get("cancel", currentLanguage))
                                }
                                Button(
                                    onClick = { onConfirmAction(pending) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("voice_action_confirm_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopGreen)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = StringsProvider.get("confirm", currentLanguage))
                                }
                            }
                        }
                    }
                }

                // Error message
                if (voiceState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = voiceState.errorMessage,
                        fontSize = 13.sp,
                        color = ShopRed,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Prompt Chips
                Text(
                    text = "Try asking (ఒకసారి అడగండి):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sampleQueries) { q ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ShopNavy.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    inputQueryText = q
                                    onQuerySelect(q)
                                }
                        ) {
                            Text(
                                text = q,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ShopNavy,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
