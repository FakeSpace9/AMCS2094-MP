package com.example.miniproject.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.data.entity.CustomerEntity
import com.example.miniproject.repository.EditProfileRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repo: EditProfileRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    var name = mutableStateOf("")
    var phone = mutableStateOf("")
    var email = mutableStateOf("")
    var message = mutableStateOf("")

    private var currentCustomerId: String = ""

    fun isValidPhone(phone: String): Boolean {
        return Regex("^01\\d{8,9}$").matches(phone)
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateProfile(): String? {
        if (name.value.isBlank()) return "Name cannot be empty"
        if (email.value.isBlank()) return "Email cannot be empty"
        if (phone.value.isBlank()) return "Phone Number cannot be empty"
        if (name.value.length < 5) return "Name cannot be less than 5 letters"
        if (!isValidEmail(email.value)) return "Invalid Email"
        if (!isValidPhone(phone.value)) return "Invalid Phone Number"

        return null
    }


    fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = authPrefs.getUserId() ?: return@launch

            val user = repo.getCustomerById(userId)

            name.value = user.name
            phone.value = user.phone
            email.value = user.email
            currentCustomerId = user.customerId
        }
    }

    fun saveProfile(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {

            val error = validateProfile()
            if (error != null) {
                message.value = error
                onResult(false)
                return@launch
            }

            val updated = CustomerEntity(
                customerId = currentCustomerId,
                name = name.value,
                phone = phone.value,
                email = email.value
            )

            val result = repo.updateProfile(updated)

            if (result.isSuccess) {
                message.value = "Profile Saved"
                onResult(true)
            } else {
                message.value = "Failed: ${result.exceptionOrNull()?.message}"
                onResult(false)
            }
        }
    }
}