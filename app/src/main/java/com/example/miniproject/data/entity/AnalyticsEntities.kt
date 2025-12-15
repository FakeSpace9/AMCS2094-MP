package com.example.miniproject.data.entity

data class BestSellerItem(
    val name: String,
    val imageUrl: String?,
    val totalQty: Int,
    val totalPrice: Double
)

enum class AnalyticsTab {
    ONLINE, PHYSICAL, SUMMARY
}

data class DashboardStats(
    val revenue: Double,
    val orders: Int,
    val itemsSold: Int,
    val bestSellers: List<BestSellerItem>
)