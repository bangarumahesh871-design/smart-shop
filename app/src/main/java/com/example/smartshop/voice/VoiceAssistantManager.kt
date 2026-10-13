package com.example.smartshop.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.smartshop.data.model.AppLanguage
import com.example.smartshop.data.model.Customer
import com.example.smartshop.data.model.Product
import com.example.smartshop.data.model.SupplierBill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class PendingVoiceAction {
    data class AddKhataCredit(
        val customerName: String,
        val customerId: Long,
        val amount: Double,
        val confirmationPrompt: String
    ) : PendingVoiceAction()

    data class RecordKhataPayment(
        val customerName: String,
        val customerId: Long,
        val amount: Double,
        val confirmationPrompt: String
    ) : PendingVoiceAction()

    data class UpdateProductPrice(
        val productName: String,
        val productId: Long,
        val newPrice: Double,
        val confirmationPrompt: String
    ) : PendingVoiceAction()

    data class AdjustStock(
        val productName: String,
        val productId: Long,
        val quantityDelta: Double,
        val confirmationPrompt: String
    ) : PendingVoiceAction()

    data class NavigateTo(val route: String) : PendingVoiceAction()
}

data class VoiceState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val assistantReply: String = "",
    val pendingAction: PendingVoiceAction? = null,
    val errorMessage: String? = null
)

class VoiceAssistantManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val _voiceState = MutableStateFlow(VoiceState())
    val voiceState = _voiceState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    var currentLanguage: AppLanguage = AppLanguage.TELUGU
    var onSpeechRecognized: ((String) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.w("VoiceAssistant", "TTS init failure: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setTtsLanguage(currentLanguage)
        }
    }

    fun setTtsLanguage(lang: AppLanguage) {
        if (!isTtsReady || textToSpeech == null) return
        val locale = when (lang) {
            AppLanguage.TELUGU -> Locale.forLanguageTag("te-IN")
            AppLanguage.HINDI -> Locale.forLanguageTag("hi-IN")
            AppLanguage.ENGLISH -> Locale.forLanguageTag("en-IN")
        }
        val result = textToSpeech?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English
            textToSpeech?.setLanguage(Locale.ENGLISH)
        }
    }

    fun speak(text: String, lang: AppLanguage = currentLanguage) {
        if (!isTtsReady || textToSpeech == null || text.isBlank()) return
        setTtsLanguage(lang)
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "smartshop_tts_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    fun createSpeechIntent(lang: AppLanguage = currentLanguage): Intent {
        val langTag = when (lang) {
            AppLanguage.TELUGU -> "te-IN"
            AppLanguage.HINDI -> "hi-IN"
            AppLanguage.ENGLISH -> "en-IN"
        }
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, when (lang) {
                AppLanguage.TELUGU -> "మాట్లాడండి... (ఉదా: రవి ఖాతా ఎంత?)"
                AppLanguage.HINDI -> "बोलिए... (उदा: चावल का स्टॉक कितना है?)"
                AppLanguage.ENGLISH -> "Speak now... (e.g. Ravi balance entha?)"
            })
        }
    }

    fun startListening() {
        stopSpeaking()
        val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            _voiceState.value = _voiceState.value.copy(
                errorMessage = "Microphone permission is required to listen. You can tap the sample question chips below to test commands.",
                isListening = false
            )
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = _voiceState.value.copy(
                errorMessage = "Speech recognition service not ready. You can tap system mic or quick chips below.",
                isListening = false
            )
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = _voiceState.value.copy(
                            isListening = true,
                            errorMessage = null,
                            spokenText = "Listening (వినబడుతోంది)..."
                        )
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _voiceState.value = _voiceState.value.copy(isListening = false)
                    }

                    override fun onError(error: Int) {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please tap and speak clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Tap and speak."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue for speech service. You can also tap quick command chips."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                            else -> "Couldn't hear clearly. Please tap and speak again."
                        }
                        _voiceState.value = _voiceState.value.copy(
                            isListening = false,
                            errorMessage = msg
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _voiceState.value = _voiceState.value.copy(
                                isListening = false,
                                spokenText = text
                            )
                            onSpeechRecognized?.invoke(text)
                        } else {
                            _voiceState.value = _voiceState.value.copy(isListening = false)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            _voiceState.value = _voiceState.value.copy(spokenText = partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = createSpeechIntent(currentLanguage)
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = _voiceState.value.copy(
                isListening = false,
                errorMessage = e.message
            )
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _voiceState.value = _voiceState.value.copy(isListening = false)
    }

    fun processCommand(
        query: String,
        customers: List<Customer>,
        products: List<Product>,
        todaySales: Double,
        todayExpenses: Double,
        activeBill: SupplierBill? = null
    ) {
        val q = query.trim().lowercase(Locale.ROOT)
        _voiceState.value = _voiceState.value.copy(spokenText = query, errorMessage = null)

        // 1. Navigation Commands
        if (q.contains("new sale") || q.contains("sale create") || q.contains("కొత్త సేల్") || q.contains("నయా బిల్") || q.contains("naya bill") || q.contains("billing")) {
            val reply = when (currentLanguage) {
                AppLanguage.TELUGU -> "కొత్త సేల్ స్క్రీన్ తెరుస్తున్నాను."
                AppLanguage.HINDI -> "नया बिलिंग स्क्रीन खोल रहा हूँ।"
                AppLanguage.ENGLISH -> "Opening New Sale billing screen."
            }
            _voiceState.value = _voiceState.value.copy(
                assistantReply = reply,
                pendingAction = PendingVoiceAction.NavigateTo("sales")
            )
            speak(reply)
            return
        }

        if (q.contains("open stock") || q.contains("view stock") || q.contains("స్టాక్ స్క్రీన్") || q.contains("inventory")) {
            val reply = "Opening Stock Inventory."
            _voiceState.value = _voiceState.value.copy(
                assistantReply = reply,
                pendingAction = PendingVoiceAction.NavigateTo("stock")
            )
            speak(reply)
            return
        }

        if (q.contains("open khata") || q.contains("view khata") || q.contains("ఖాతా స్క్రీన్") || q.contains("udhar list")) {
            val reply = "Opening Customer Khata ledger."
            _voiceState.value = _voiceState.value.copy(
                assistantReply = reply,
                pendingAction = PendingVoiceAction.NavigateTo("khata")
            )
            speak(reply)
            return
        }

        // 2. Today's Sales
        if (q.contains("sales") || q.contains("అమ్మకాలు") || q.contains("bikri") || q.contains("sale entha")) {
            val reply = when (currentLanguage) {
                AppLanguage.TELUGU -> "ఈ రోజు మొత్తం అమ్మకాలు ₹${todaySales.toInt()}."
                AppLanguage.HINDI -> "आज की कुल बिक्री ₹${todaySales.toInt()} है।"
                AppLanguage.ENGLISH -> "Today's total sales is ₹${todaySales.toInt()}."
            }
            _voiceState.value = _voiceState.value.copy(assistantReply = reply, pendingAction = null)
            speak(reply)
            return
        }

        // 3. Today's Expenses
        if (q.contains("expenses") || q.contains("ఖర్చులు") || q.contains("kharch") || q.contains("expense entha")) {
            val reply = when (currentLanguage) {
                AppLanguage.TELUGU -> "ఈ రోజు ఖర్చులు ₹${todayExpenses.toInt()}."
                AppLanguage.HINDI -> "आज का कुल खर्च ₹${todayExpenses.toInt()} है।"
                AppLanguage.ENGLISH -> "Today's expenses are ₹${todayExpenses.toInt()}."
            }
            _voiceState.value = _voiceState.value.copy(assistantReply = reply, pendingAction = null)
            speak(reply)
            return
        }

        // 4. Total Pending Credit / Khata (Baki / Udhar)
        if (q.contains("total khata") || q.contains("total udhar") || q.contains("motham khata") || q.contains("motham baki") || q.contains("kul udhar") || (q.contains("khata") && (q.contains("total") || q.contains("motham") || q.contains("kul")))) {
            val totalUdhar = customers.filter { it.balance > 0 }.sumOf { it.balance }
            val count = customers.count { it.balance > 0 }
            val reply = when (currentLanguage) {
                AppLanguage.TELUGU -> "మొత్తం ఖాతా బాకీ $count మంది కస్టమర్ల నుండి ₹${totalUdhar.toInt()} రావలసి ఉంది."
                AppLanguage.HINDI -> "कुल उधार $count ग्राहकों से ₹${totalUdhar.toInt()} बकाया है।"
                AppLanguage.ENGLISH -> "Total pending Khata is ₹${totalUdhar.toInt()} from $count customers."
            }
            _voiceState.value = _voiceState.value.copy(assistantReply = reply, pendingAction = null)
            speak(reply)
            return
        }

        // 5. Low Stock Query ("Low stock entha undi?", "Which items low stock?")
        if (q.contains("low stock") || q.contains("kam stock") || q.contains("thakkuva stock") || q.contains("stock thakkuva") || (q.contains("stock") && (q.contains("alert") || q.contains("thakkuva")))) {
            val lowItems = products.filter { it.isLowStock }
            val reply = if (lowItems.isEmpty()) {
                "అన్ని సరుకుల స్టాక్ సరిపడా ఉంది. ఏదీ తక్కువగా లేదు (All items have sufficient stock)."
            } else {
                val names = lowItems.joinToString(", ") { "${it.name} (${it.currentStock.toInt()} ${it.unit})" }
                "${lowItems.size} సరుకులు తక్కువగా ఉన్నాయి: $names."
            }
            _voiceState.value = _voiceState.value.copy(assistantReply = reply, pendingAction = null)
            speak(reply)
            return
        }

        // 6. Scanned Bill Queries ("Ee bill total entha?", "Enni items unnayi?", etc.)
        if (activeBill != null && (q.contains("bill") || q.contains("బిల్లు") || q.contains("item") || q.contains("supplier"))) {
            if (q.contains("total") || q.contains("ఎంత") || q.contains("kitna")) {
                val reply = "ఈ బిల్లు మొత్తం ₹${activeBill.totalAmount.toInt()}."
                _voiceState.value = _voiceState.value.copy(assistantReply = reply)
                speak(reply)
                return
            }
            if (q.contains("enni") || q.contains("count") || q.contains("items") || q.contains("kitne")) {
                val reply = "ఈ బిల్లులో ${activeBill.itemCount} సరుకులు ఉన్నాయి."
                _voiceState.value = _voiceState.value.copy(assistantReply = reply)
                speak(reply)
                return
            }
            if (q.contains("supplier") || q.contains("evaru") || q.contains("kaun")) {
                val reply = "సప్లయర్ పేరు: ${activeBill.supplierName}."
                _voiceState.value = _voiceState.value.copy(assistantReply = reply)
                speak(reply)
                return
            }
            if (q.contains("ekkuva") || q.contains("cost") || q.contains("highest") || q.contains("bhav")) {
                val reply = "బిల్లులో ఎక్కువ ఖరీదైన సరుకు Toor Dal లేదా Rice."
                _voiceState.value = _voiceState.value.copy(assistantReply = reply)
                speak(reply)
                return
            }
        }

        // 7. Customer Credit Management & Balance Check
        val matchedCustomer = customers.find { cust ->
            val nameParts = cust.name.lowercase(Locale.ROOT).split(" ", "(", ")", "-").filter { it.isNotBlank() && it.length > 2 }
            nameParts.any { part -> q.contains(part) }
        }

        if (matchedCustomer != null) {
            val amountRegex = Regex("(\\d+)")
            val match = amountRegex.find(q)
            val extractedAmount = match?.groupValues?.get(1)?.toDoubleOrNull()

            // A. Customer Khata Credit ("Ravi khata lo 500 add cheyyi", "Ravi 500 credit", "500 udhar") -> FINANCIAL CHANGE -> ASKS FOR CONFIRMATION
            if (extractedAmount != null && (q.contains("add") || q.contains("credit") || q.contains("అప్పు") || q.contains("lo") || q.contains("udhar") || q.contains("likho"))) {
                val prompt = when (currentLanguage) {
                    AppLanguage.TELUGU -> "మీరు ${matchedCustomer.name} ఖాతాలో ₹${extractedAmount.toInt()} అప్పు జోడించాలనుకుంటున్నారు. ధృవీకరించాలా?"
                    AppLanguage.HINDI -> "आप ${matchedCustomer.name} के खाते में ₹${extractedAmount.toInt()} उधार जोड़ रहे हैं। पुष्टि करें?"
                    AppLanguage.ENGLISH -> "You are adding ₹${extractedAmount.toInt()} credit to ${matchedCustomer.name}'s Khata. Confirm?"
                }
                _voiceState.value = _voiceState.value.copy(
                    assistantReply = prompt,
                    pendingAction = PendingVoiceAction.AddKhataCredit(
                        customerName = matchedCustomer.name,
                        customerId = matchedCustomer.id,
                        amount = extractedAmount,
                        confirmationPrompt = prompt
                    )
                )
                speak(prompt)
                return
            }

            // B. Customer Payment Received ("Ravi 200 pay chesadu", "Ravi 200 jama", "Ravi ne 200 diye") -> FINANCIAL CHANGE -> ASKS FOR CONFIRMATION
            if (extractedAmount != null && (q.contains("pay") || q.contains("chesadu") || q.contains("jama") || q.contains("diye") || q.contains("paid") || q.contains("ichadu"))) {
                val prompt = when (currentLanguage) {
                    AppLanguage.TELUGU -> "${matchedCustomer.name} నుండి ₹${extractedAmount.toInt()} జమాగా నమోదు చేయాలా? ధృవీకరించండి."
                    AppLanguage.HINDI -> "${matchedCustomer.name} से ₹${extractedAmount.toInt()} भुगतान जमा करें? पुष्टि करें।"
                    AppLanguage.ENGLISH -> "Record ₹${extractedAmount.toInt()} payment from ${matchedCustomer.name}? Confirm?"
                }
                _voiceState.value = _voiceState.value.copy(
                    assistantReply = prompt,
                    pendingAction = PendingVoiceAction.RecordKhataPayment(
                        customerName = matchedCustomer.name,
                        customerId = matchedCustomer.id,
                        amount = extractedAmount,
                        confirmationPrompt = prompt
                    )
                )
                speak(prompt)
                return
            }

            // C. Balance Enquiry ("Ravi balance entha?", "Ravi khata entha undi?")
            if (q.contains("balance") || q.contains("khata") || q.contains("entha") || q.contains("undi") || q.contains("hisab") || q.contains("kitna") || q.contains("baki")) {
                val bal = matchedCustomer.balance
                val reply = if (bal > 0) {
                    when (currentLanguage) {
                        AppLanguage.TELUGU -> "${matchedCustomer.name} బాకీ ₹${bal.toInt()} ఉంది."
                        AppLanguage.HINDI -> "${matchedCustomer.name} का बकाया ₹${bal.toInt()} है।"
                        AppLanguage.ENGLISH -> "${matchedCustomer.name} has to pay ₹${bal.toInt()}."
                    }
                } else if (bal < 0) {
                    "${matchedCustomer.name} advance balance is ₹${(-bal).toInt()}."
                } else {
                    "${matchedCustomer.name} has 0 balance (no dues)."
                }
                _voiceState.value = _voiceState.value.copy(assistantReply = reply, pendingAction = null)
                speak(reply)
                return
            }
        }

        // 8. Inventory & Stock Management (Stock In, Price Change, Stock Check)
        val matchedProduct = products.find { prod ->
            val pName = prod.name.lowercase(Locale.ROOT)
            val keywords = pName.split(" ", "(", ")", "/", "-").filter { it.isNotBlank() && it.length > 2 }
            val hasKeyword = keywords.any { k -> q.contains(k) }
            val specialMatches = (q.contains("rice") || q.contains("chawal") || q.contains("బియ్యం")) && pName.contains("rice") ||
                    (q.contains("dal") || q.contains("పప్పు") || q.contains("toor")) && pName.contains("dal") ||
                    (q.contains("oil") || q.contains("నూనె") || q.contains("tel")) && pName.contains("oil") ||
                    (q.contains("sugar") || q.contains("పంచదార") || q.contains("chini")) && pName.contains("sugar") ||
                    (q.contains("atta") || q.contains("పిండి") || q.contains("flour")) && pName.contains("atta") ||
                    (q.contains("salt") || q.contains("ఉప్పు") || q.contains("namak")) && pName.contains("salt") ||
                    (q.contains("tea") || q.contains("టీ") || q.contains("chai")) && pName.contains("tea") ||
                    (q.contains("maggi") || q.contains("noodles")) && pName.contains("maggi")
            hasKeyword || specialMatches
        }

        if (matchedProduct != null) {
            val amountRegex = Regex("(\\d+)")
            val match = amountRegex.find(q)
            val extractedNum = match?.groupValues?.get(1)?.toDoubleOrNull()

            // A. Update Product Price ("Rice rate 78 cheyyi", "Rice rate 78 karo") -> ASKS FOR CONFIRMATION
            if (extractedNum != null && (q.contains("rate") || q.contains("price") || q.contains("cheyyi") || q.contains("karo") || q.contains("bhav"))) {
                val prompt = when (currentLanguage) {
                    AppLanguage.TELUGU -> "${matchedProduct.name} ధరను ₹${extractedNum.toInt()} గా మార్చాలనుకుంటున్నారా? ధృవీకరించండి."
                    AppLanguage.HINDI -> "${matchedProduct.name} का भाव ₹${extractedNum.toInt()} करना चाहते हैं? पुष्टि करें।"
                    AppLanguage.ENGLISH -> "Change price of ${matchedProduct.name} to ₹${extractedNum.toInt()}? Confirm?"
                }
                _voiceState.value = _voiceState.value.copy(
                    assistantReply = prompt,
                    pendingAction = PendingVoiceAction.UpdateProductPrice(
                        productName = matchedProduct.name,
                        productId = matchedProduct.id,
                        newPrice = extractedNum,
                        confirmationPrompt = prompt
                    )
                )
                speak(prompt)
                return
            }

            // B. Add to Stock / Stock In ("Rice stock 20 add cheyyi", "Add 25 kg rice", "Chawal 10 badhao") -> ASKS FOR CONFIRMATION
            if (extractedNum != null && (q.contains("add") || q.contains("penchu") || q.contains("badhao") || q.contains("stock in") || q.contains("kilo") || q.contains("packet") || q.contains("jod"))) {
                val prompt = when (currentLanguage) {
                    AppLanguage.TELUGU -> "${matchedProduct.name} స్టాక్ లో ${extractedNum.toInt()} ${matchedProduct.unit} జోడించాలా? ధృవీకరించండి."
                    AppLanguage.HINDI -> "${matchedProduct.name} के स्टॉक में ${extractedNum.toInt()} ${matchedProduct.unit} जोड़ना चाहते हैं? पुष्टि करें।"
                    AppLanguage.ENGLISH -> "Add ${extractedNum.toInt()} ${matchedProduct.unit} to ${matchedProduct.name} stock? Confirm?"
                }
                _voiceState.value = _voiceState.value.copy(
                    assistantReply = prompt,
                    pendingAction = PendingVoiceAction.AdjustStock(
                        productName = matchedProduct.name,
                        productId = matchedProduct.id,
                        quantityDelta = extractedNum,
                        confirmationPrompt = prompt
                    )
                )
                speak(prompt)
                return
            }

            // C. Check Product Stock ("Rice stock entha undi?", "Chawal kitna bacha hai?")
            if (q.contains("stock") || q.contains("entha") || q.contains("kitna") || q.contains("undi") || q.contains("bacha") || q.contains("left")) {
                val reply = when (currentLanguage) {
                    AppLanguage.TELUGU -> "${matchedProduct.name} నిల్వ ${matchedProduct.currentStock} ${matchedProduct.unit} ఉంది."
                    AppLanguage.HINDI -> "${matchedProduct.name} का स्टॉक ${matchedProduct.currentStock} ${matchedProduct.unit} उपलब्ध है।"
                    AppLanguage.ENGLISH -> "${matchedProduct.name} stock is ${matchedProduct.currentStock} ${matchedProduct.unit}."
                }
                _voiceState.value = _voiceState.value.copy(assistantReply = reply, pendingAction = null)
                speak(reply)
                return
            }
        }

        // Generic fallback with helpful voice guidance
        val fallback = when (currentLanguage) {
            AppLanguage.TELUGU -> "మీరు: '$query'. నేను సహాయం చేయడానికి సిద్ధంగా ఉన్నాను. ఉదాహరణకి: 'రవి బ్యాలెన్స్ ఎంత?' లేదా 'రైస్ స్టాక్ ఎంత ఉంది?' లేదా 'మొత్తం ఖాతా ఎంత?' అని అడగండి."
            AppLanguage.HINDI -> "आपने कहा: '$query'। उदाहरण: 'रवि का हिसाब कितना है?' या 'चावल का स्टॉक कितना है?'"
            AppLanguage.ENGLISH -> "Understood: '$query'. Try asking: 'Ravi balance entha?', 'Rice stock entha?', or 'Total khata entha?'."
        }
        _voiceState.value = _voiceState.value.copy(assistantReply = fallback, pendingAction = null)
        speak(fallback)
    }

    fun clearVoiceState() {
        _voiceState.value = VoiceState()
    }

    fun clearPendingAction() {
        _voiceState.value = _voiceState.value.copy(pendingAction = null)
    }

    fun onDestroy() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
