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

    var paymentId: Long? = null
    var paymentType = mutableStateOf("CARD")   // default type

    var displayName = mutableStateOf("")
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

    fun loadPayments() {
        viewModelScope.launch {
            customerId = authPrefs.getUserId() ?: return@launch
            _payments.value = repo.getPayments(customerId)
        }
    }

    fun startNew() {
        paymentId = null
        paymentType.value = "CARD"
        displayName.value = ""
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
            displayName.value = p.displayName
            cardHolderName.value = p.cardHolderName ?: ""
            cardNumber.value = p.cardNumber ?: ""
            expiryMonth.value = p.expiryMonth?.toString() ?: ""
            expiryYear.value = p.expiryYear?.toString() ?: ""
            cvv.value = p.cvv ?: ""
            walletId.value = p.walletId ?: ""
            isDefault.value = p.isDefault
        }
    }

    fun save(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            customerId = authPrefs.getUserId() ?: return@launch

            val payment = PaymentEntity(
                paymentId = paymentId ?: 0,
                customerId = customerId,
                paymentType = paymentType.value,
                displayName = displayName.value,
                cardHolderName = cardHolderName.value,
                cardNumber = cardNumber.value,
                expiryMonth = expiryMonth.value.toIntOrNull(),
                expiryYear = expiryYear.value.toIntOrNull(),
                cvv = cvv.value,
                walletId = walletId.value,
                isDefault = isDefault.value
            )

            val result = repo.savePayment(payment)

            if (result.isSuccess) {
                val newId = result.getOrNull()!!
                if (isDefault.value) repo.setDefault(customerId, newId)
                loadPayments()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun delete(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            customerId = authPrefs.getUserId() ?: return@launch
            val result = repo.deletePayment(customerId, id)
            if (result.isSuccess) {
                loadPayments()
                onResult(true)
            } else onResult(false)
        }
    }
}
