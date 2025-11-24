package com.example.miniproject.viewmodel

import LoginRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.AuthPreferences

class LoginViewModelFactory(
    private val repository: LoginRepository,
    private val authPrefs: AuthPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository, authPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
