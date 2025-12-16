package com.example.miniproject.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.PaymentEntity
import com.example.miniproject.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val repo: PaymentRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    private val _payments = MutableStateFlow<List<PaymentEntity>>(emptyList())
    val payments: StateFlow<List<PaymentEntity>> = _payments

    var errorMessage = mutableStateOf<String?>(null)
    var paymentId: Long? = null
    var paymentType = mutableStateOf("CARD")   // default type
    var cardName = mutableStateOf("")
    var cardHolderName = mutableStateOf("")
    var cardNumber = mutableStateOf("")
    var expiryMonth = mutableStateOf("")
    var expiryYear = mutableStateOf("")
    var cvv = mutableStateOf("")
    var walletId = mutableStateOf("")
    var isDefault = mutableStateOf(false)

    private var customerId: String = ""

    init {
        loadPayments()
    }

    fun isValidTNG(phone: String): Boolean {
        return Regex("^01\\d{8,9}$").matches(phone)
    }

    fun isValidExpiryMonth(month: String): Boolean {
        val m = month.toIntOrNull() ?: return false
        return m in 1..12
    }

    fun isValidExpiryYear(year: String): Boolean {
        val yInput = year.toIntOrNull() ?: return false

        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentYear2Digit = currentYear % 100

        return when (year.length) {
            2 -> yInput >= currentYear2Digit
            4 -> yInput >= currentYear
            else -> false
        }
    }

    fun isValidCVV(cvv: String): Boolean {
        return Regex("^\\d{3}$").matches(cvv)
    }

    fun isCardNotExpired(month: String, year: String): Boolean {
        val m = month.toIntOrNull() ?: return false
        val yInput = year.toIntOrNull() ?: return false

        if (m !in 1..12) return false

        val y = if (year.length == 2) {
            2000 + yInput
        } else {
            yInput
        }

        val now = java.util.Calendar.getInstance()
        val currentYear = now.get(java.util.Calendar.YEAR)
        val currentMonth = now.get(java.util.Calendar.MONTH) + 1

        return when {
            y > currentYear -> true
            y == currentYear && m >= currentMonth -> true
            else -> false
        }
    }

    fun validatePayment(): String? {
        return when (paymentType.value) {

            "CARD" -> {
                if (cardName.value.isBlank()) return "Card name cannot be empty"
                if (cardHolderName.value.isBlank()) return "Card holder name cannot be empty"
                if (cardNumber.value.isBlank()) return "Card number cannot be empty"
                if (expiryMonth.value.isBlank()) return "Expiry month cannot be empty"
                if (expiryYear.value.isBlank()) return "Expiry year cannot be empty"
                if (cvv.value.isBlank()) return "CVV cannot be empty"

                if (!isValidExpiryMonth(expiryMonth.value)) return "Invalid expiry month"
                if (!isValidExpiryYear(expiryYear.value)) return "Invalid expiry year"
                if (!isCardNotExpired(expiryMonth.value, expiryYear.value)) return "Card has expired"
                if (!isValidCVV(cvv.value)) return "CVV must be 3 digits"

                null
            }

            "TNG" -> {
                if (walletId.value.isBlank()) return "Phone number cannot be empty"
                if (!isValidTNG(walletId.value)) return "Invalid Touch 'n Go number"
                null
            }

            else -> "Invalid payment type"
        }
    }

    fun loadPayments() {
        viewModelScope.launch {
            customerId = authPrefs.getUserId() ?: return@launch
            _payments.value = repo.getPayments(customerId)
        }
    }

    fun newPayment() {
        paymentId = null
        paymentType.value = "CARD"
        cardName.value = ""
        cardHolderName.value = ""
        cardNumber.value = ""
        expiryMonth.value = ""
        expiryYear.value = ""
        cvv.value = ""
        walletId.value = ""
        isDefault.value = false

    }

    fun editPayment(id: Long) {
        viewModelScope.launch {
            val p = repo.getPaymentById(id) ?: return@launch

            paymentId = p.paymentId
            paymentType.value = p.paymentType
            cardName.value = p.displayName
            cardHolderName.value = p.cardHolderName ?: ""
            cardNumber.value = p.cardNumber ?: ""
            expiryMonth.value = p.expiryMonth?.toString() ?: ""
            expiryYear.value = p.expiryYear?.toString() ?: ""
            cvv.value = p.cvv ?: ""
            walletId.value = p.walletId ?: ""
            isDefault.value = p.isDefault
        }
    }


    fun savePayment(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {

            customerId = authPrefs.getUserId() ?: run {
                onResult(false)
                return@launch
            }

            val error = validatePayment()
            if (error != null) {
                errorMessage.value = error
                onResult(false)
                return@launch
            }


            val payment = PaymentEntity(
                paymentId = paymentId ?: 0,
                customerId = customerId,
                paymentType = paymentType.value,
                displayName = cardName.value,
                cardHolderName = cardHolderName.value.takeIf { paymentType.value == "CARD" },
                cardNumber = cardNumber.value.takeIf { paymentType.value == "CARD" },
                expiryMonth = expiryMonth.value.toIntOrNull(),
                expiryYear = expiryYear.value.toIntOrNull(),
                cvv = cvv.value.takeIf { paymentType.value == "CARD" },
                walletId = walletId.value.takeIf { paymentType.value == "TNG" },
                isDefault = isDefault.value
            )

            val result = repo.savePayment(payment)

            if (result.isSuccess) {
                val newId = result.getOrNull()!!
                if (isDefault.value) repo.setDefaultPayment(customerId, newId)
                loadPayments()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }


    fun deletePayment(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            customerId = authPrefs.getUserId() ?: return@launch
            val result = repo.deletePayment(customerId, id)
            if (result.isSuccess) {
                loadPayments()
                onResult(true)
            } else onResult(false)
        }
    }

    fun syncPayments() {
        viewModelScope.launch {
            val userId = authPrefs.getUserId() ?: return@launch

            repo.getPaymentsFromFirebase(userId)
            loadPayments()
        }
    }

}
