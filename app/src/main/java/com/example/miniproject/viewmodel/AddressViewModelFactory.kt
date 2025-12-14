package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.AddressRepository

class AddressViewModelFactory(
    private val repo: AddressRepository,
    private val authPrefs: AuthPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddressViewModel(repo, authPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
