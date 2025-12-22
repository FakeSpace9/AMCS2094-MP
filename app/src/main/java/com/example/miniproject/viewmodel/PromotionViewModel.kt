package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.AuthPreferences
import com.example.miniproject.repository.PromotionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PromotionViewModel(
    private val repository: PromotionRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    val promotions = repository.allPromotions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun syncPromotions() {
        viewModelScope.launch {
            repository.syncPromotions()
        }
    }

    fun createPromo(
        code: String,
        name: String,
        desc: String,
        rateStr: String,
        daysValid: Int,
        onResult: (String?) -> Unit
    ) {
        val adminId = authPrefs.getUserId()
        if (adminId == null) {
            onResult("Error: Admin not logged in")
            return
        }

        val rate = rateStr.toDoubleOrNull()
        if (rate == null || rate <= 0) {
            onResult("Invalid discount rate")
            return
        }

        val start = System.currentTimeMillis()
        val end = start + (daysValid * 24 * 60 * 60 * 1000L)

        viewModelScope.launch {
            val result = repository.createPromotion(adminId, code, name, desc, rate, start, end)
            if (result.isSuccess) onResult(null) // Success
            else onResult(result.exceptionOrNull()?.message)
        }
    }

    fun deletePromo(id: String) {
        viewModelScope.launch { repository.deletePromotion(id) }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

class PromotionViewModelFactory(
    private val repo: PromotionRepository,
    private val auth: AuthPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PromotionViewModel(repo, auth) as T
    }
}