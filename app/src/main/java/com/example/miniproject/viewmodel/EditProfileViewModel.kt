package com.example.miniproject.viewmodel

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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

    // Current State
    var profilePicture = mutableStateOf<String?>(null)
    var name = mutableStateOf("")
    var phone = mutableStateOf("")
    var email = mutableStateOf("")
    var message = mutableStateOf("")

    // Original State (to check for changes)
    private var originalProfilePicture: String? = null
    private var originalName: String = ""
    private var originalPhone: String = ""
    private var currentCustomerId: String = ""

    // --- NEW: Check if anything changed ---
    val isModified by derivedStateOf {
        name.value != originalName ||
                phone.value != originalPhone ||
                profilePicture.value != originalProfilePicture
    }

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

            if (user != null) {
                // Set Current Values
                name.value = user.name
                phone.value = user.phone
                email.value = user.email
                profilePicture.value = user.profilePictureUrl
                currentCustomerId = user.customerId

                // Set Original Values
                originalName = user.name
                originalPhone = user.phone
                originalProfilePicture = user.profilePictureUrl
            } else {
                message.value = "User data not found"
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        profilePicture.value = uri.toString()
    }

    fun onPredefinedImageSelected(code: String) {
        profilePicture.value = code
    }

    fun changePassword(currentPass: String, newPass: String, onSuccess: () -> Unit) {
        if (currentPass.isBlank() || newPass.isBlank()) {
            message.value = "Passwords cannot be empty"
            return
        }
        if (newPass.length < 6) {
            message.value = "New password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            val result = repo.changePassword(currentPass, newPass)
            if (result.isSuccess) {
                onSuccess() // Trigger UI to handle logout
            } else {
                message.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveProfile(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Validation omitted for brevity, assumes standard checks...
            if (name.value.isBlank()) { message.value = "Name required"; onResult(false); return@launch }

            var finalImageUrl = profilePicture.value
            if (finalImageUrl != null && finalImageUrl!!.startsWith("content://")) {
                val upload = repo.uploadProfilePicture(Uri.parse(finalImageUrl))
                if (upload.isSuccess) finalImageUrl = upload.getOrNull()
                else { message.value = "Upload failed"; onResult(false); return@launch }
            }

            val updated = CustomerEntity(currentCustomerId, email.value, name.value, phone.value, finalImageUrl)
            val result = repo.updateProfile(updated)

            if (result.isSuccess) {
                originalName = name.value
                originalPhone = phone.value
                originalProfilePicture = finalImageUrl
                message.value = "Profile Saved"
                onResult(true)
            } else {
                message.value = "Failed: ${result.exceptionOrNull()?.message}"
                onResult(false)
            }
        }
    }
}