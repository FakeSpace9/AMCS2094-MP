package com.example.miniproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.AnalyticsTab
import com.example.miniproject.data.entity.DashboardStats
import com.example.miniproject.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AnalyticsViewModel(
    private val repository: AnalyticsRepository
) : ViewModel() {

    // Filters
    val selectedTab = MutableStateFlow(AnalyticsTab.ONLINE)
    val selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    // Data State
    private val _stats = MutableStateFlow<DashboardStats?>(null)
    val stats: StateFlow<DashboardStats?> = _stats

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val years = (2024..2030).map { it.toString() }

    init {
        // --- CHANGE: Trigger Sync First ---
        viewModelScope.launch {
            repository.refreshData() // Download latest data
            loadData()               // Load from Room
        }
    }

    fun setTab(tab: AnalyticsTab) {
        selectedTab.value = tab
        loadData()
    }

    fun setDate(monthIndex: Int, year: Int) {
        selectedMonth.value = monthIndex
        selectedYear.value = year
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _stats.value = repository.getStats(
                selectedTab.value,
                selectedMonth.value,
                selectedYear.value
            )
        }
    }
}

class AnalyticsViewModelFactory(private val repository: AnalyticsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AnalyticsViewModel(repository) as T
    }
}