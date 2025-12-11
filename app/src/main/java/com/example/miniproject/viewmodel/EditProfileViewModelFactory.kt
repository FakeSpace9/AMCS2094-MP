package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.EditProfileRepository

class EditProfileViewModelFactory(
    private val repo: EditProfileRepository,
    private val authPrefs: AuthPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(repo, authPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
