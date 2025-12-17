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
    var isGoogleAccount = mutableStateOf(false) // <--- Track Google User

    private var originalProfilePicture: String? = null
    private var originalName: String = ""
    private var originalPhone: String = ""
    private var currentCustomerId: String = ""

    val isModified by derivedStateOf {
        name.value != originalName ||
                phone.value != originalPhone ||
                profilePicture.value != originalProfilePicture
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            // Check provider type immediately
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

    // --- CHANGE PASSWORD LOGIC ---
    fun resetPasswordFields() {
        currentPassword.value = ""
        newPassword.value = ""
        confirmPassword.value = ""
        message.value = ""
    }

    fun changePassword(onSuccess: () -> Unit) {
        val current = currentPassword.value
        val newPass = newPassword.value
        val confirm = confirmPassword.value

        if (current.isBlank() || newPass.isBlank() || confirm.isBlank()) {
            message.value = "All password fields are required"
            return
        }
        if (newPass.length < 6) {
            message.value = "New password must be at least 6 characters"
            return
        }
        if (newPass != confirm) {
            message.value = "New passwords do not match"
            return
        }

        viewModelScope.launch {
            val result = repo.changePassword(current, newPass)
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