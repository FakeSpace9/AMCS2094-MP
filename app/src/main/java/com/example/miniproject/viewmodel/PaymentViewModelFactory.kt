package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.PaymentRepository

class PaymentViewModelFactory(
    private val repo: PaymentRepository,
    private val authPrefs: AuthPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(repo, authPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
