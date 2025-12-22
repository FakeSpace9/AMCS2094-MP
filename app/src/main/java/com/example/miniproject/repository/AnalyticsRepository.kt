package com.example.miniproject.repository

import com.example.miniproject.data.dao.OrderDao
import com.example.miniproject.data.dao.POSOrderDao
import com.example.miniproject.data.entity.AnalyticsTab
import com.example.miniproject.data.entity.BestSellerItem
import com.example.miniproject.data.entity.DashboardStats
import java.util.Calendar
import java.util.Date

class AnalyticsRepository(
    private val orderDao: OrderDao,
    private val posDao: POSOrderDao,
    private val orderRepo: OrderRepository,
    private val posRepo: POSRepository
) {
    suspend fun refreshData() {
        orderRepo.syncOrders()
        posRepo.syncPOSOrders()
    }
    suspend fun getStats(tab: AnalyticsTab, monthIndex: Int, year: Int): DashboardStats {
        val (start, end) = getMonthRange(monthIndex, year)
        val startDate = Date(start)
        val endDate = Date(end)

        return when (tab) {
            AnalyticsTab.ONLINE -> {
                DashboardStats(
                    revenue = orderDao.getRevenueInRange(start, end),
                    orders = orderDao.getOrderCountInRange(start, end),
                    itemsSold = orderDao.getItemsSoldInRange(start, end),
                    bestSellers = orderDao.getBestSellersInRange(start, end)
                )
            }
            AnalyticsTab.PHYSICAL -> {
                DashboardStats(
                    revenue = posDao.getRevenueInRange(startDate, endDate),
                    orders = posDao.getOrderCountInRange(startDate, endDate),
                    itemsSold = posDao.getItemsSoldInRange(startDate, endDate),
                    bestSellers = posDao.getBestSellersInRange(startDate, endDate)
                )
            }
            AnalyticsTab.SUMMARY -> {
                // Fetch both and combine
                val onlineRev = orderDao.getRevenueInRange(start, end)
                val posRev = posDao.getRevenueInRange(startDate, endDate)

                val onlineOrders = orderDao.getOrderCountInRange(start, end)
                val posOrders = posDao.getOrderCountInRange(startDate, endDate)

                val onlineItems = orderDao.getItemsSoldInRange(start, end)
                val posItems = posDao.getItemsSoldInRange(startDate, endDate)

                val onlineBest = orderDao.getBestSellersInRange(start, end)
                val posBest = posDao.getBestSellersInRange(startDate, endDate)

                val combinedBest = (onlineBest + posBest)
                    .groupBy { it.name }
                    .map { (name, list) ->
                        BestSellerItem(
                            name = name,
                            imageUrl = list.firstOrNull { !it.imageUrl.isNullOrEmpty() }?.imageUrl,
                            totalQty = list.sumOf { it.totalQty },
                            totalPrice = list.sumOf { it.totalPrice }
                        )
                    }
                    .sortedByDescending { it.totalQty }
                    .take(3)

                DashboardStats(
                    revenue = onlineRev + posRev,
                    orders = onlineOrders + posOrders,
                    itemsSold = onlineItems + posItems,
                    bestSellers = combinedBest
                )
            }
        }
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)

        // Start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        // End of month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }
}