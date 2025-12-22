package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.repository.ForgotPasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: ForgotPasswordRepository
) : ViewModel() {

    // Email state
    var email = MutableStateFlow("")
        private set

    fun onEmailChange(newEmail: String) {
        email.value = newEmail
    }

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Send reset email
    fun sendResetEmail() {
        if (email.value.isBlank()) {
            _errorMessage.value = "Email cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _successMessage.value = null
            _errorMessage.value = null

            val result = repository.sendPasswordResetEmail(email.value)

            if (result.isSuccess) {
                _successMessage.value = "Reset email sent successfully!"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send reset email"
            }

            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }
}
