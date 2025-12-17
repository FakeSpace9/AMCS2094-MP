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

    // Profile State
    var profilePicture = mutableStateOf<String?>(null)
    var name = mutableStateOf("")
    var phone = mutableStateOf("")
    var email = mutableStateOf("")

    // Password Change State
    var currentPassword = mutableStateOf("")
    var newPassword = mutableStateOf("")
    var confirmPassword = mutableStateOf("")

    // UI State
    var message = mutableStateOf("")
    var isGoogleAccount = mutableStateOf(false)

    private var originalProfilePicture: String? = null
    private var originalName: String = ""
    private var originalPhone: String = ""
    private var currentCustomerId: String = ""

    val isModified by derivedStateOf {
        name.value != originalName ||
                phone.value != originalPhone ||
                profilePicture.value != originalProfilePicture
    }

    // Password Validation
    val hasLength by derivedStateOf { newPassword.value.length >= 6 }
    val hasUppercase by derivedStateOf { newPassword.value.any { it.isUpperCase() } }
    val hasLowercase by derivedStateOf { newPassword.value.any { it.isLowerCase() } }
    val hasDigit by derivedStateOf { newPassword.value.any { it.isDigit() } }
    val hasSymbol by derivedStateOf { newPassword.value.any { !it.isLetterOrDigit() } }

    val isPasswordValid by derivedStateOf {
        hasLength && hasUppercase && hasLowercase && hasDigit && hasSymbol &&
                (newPassword.value == confirmPassword.value) && currentPassword.value.isNotEmpty()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            isGoogleAccount.value = repo.isGoogleUser()

            val userId = authPrefs.getUserId() ?: return@launch
            val user = repo.getCustomerById(userId)

            if (user != null) {
                name.value = user.name
                phone.value = user.phone
                email.value = user.email
                profilePicture.value = user.profilePictureUrl
                currentCustomerId = user.customerId

                originalName = user.name
                originalPhone = user.phone
                originalProfilePicture = user.profilePictureUrl
            } else {
                message.value = "User data not found"
            }
        }
    }

    fun onImageSelected(uri: Uri) { profilePicture.value = uri.toString() }
    fun onPredefinedImageSelected(code: String) { profilePicture.value = code }

    fun resetPasswordFields() {
        currentPassword.value = ""
        newPassword.value = ""
        confirmPassword.value = ""
        message.value = ""
    }

    fun changePassword(onSuccess: () -> Unit) {
        if (!isPasswordValid) {
            message.value = "Please ensure all requirements are met."
            return
        }

        viewModelScope.launch {
            val result = repo.changePassword(currentPassword.value, newPassword.value)
            if (result.isSuccess) {
                onSuccess()
            } else {
                message.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveProfile(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (name.value.isBlank()) { message.value = "Name required"; onResult(false); return@launch }
            if (phone.value.isBlank()) { message.value = "Phone required"; onResult(false); return@launch }

            var finalImageUrl = profilePicture.value
            if (finalImageUrl != null && finalImageUrl!!.startsWith("content://")) {
                val upload = repo.uploadProfilePicture(Uri.parse(finalImageUrl))
                if (upload.isSuccess) finalImageUrl = upload.getOrNull()
                else { message.value = "Upload failed"; onResult(false); return@launch }
            }

            // --- FIX IS HERE: Used Named Arguments ---
            val updated = CustomerEntity(
                customerId = currentCustomerId,
                name = name.value,
                email = email.value,
                phone = phone.value,
                profilePictureUrl = finalImageUrl
            )

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