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

    fun saveChanges(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val updated = CustomerEntity(
                customerId = currentCustomerId,
                name = name.value,
                phone = phone.value,
                email = email.value
            )

            val result = repo.updateProfile(updated)

            if (result.isSuccess) {
                message.value = "Profile updated"
                onResult(true)
            } else {
                message.value = "Failed: ${result.exceptionOrNull()?.message}"
                onResult(false)
            }
        }
    }
}