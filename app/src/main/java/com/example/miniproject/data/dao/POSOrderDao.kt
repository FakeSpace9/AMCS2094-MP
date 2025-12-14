package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.miniproject.data.entity.POSOrderEntity
import com.example.miniproject.data.entity.POSOrderItemEntity

@Dao
interface POSOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: POSOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<POSOrderItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<POSOrderEntity>)

    @Query("SELECT * FROM pos_orders ORDER BY orderDate DESC")
    suspend fun getAllPOSOrders(): List<POSOrderEntity>

    @Query("SELECT * FROM pos_order_items WHERE posOrderId = :orderId")
    suspend fun getPOSOrderItems(orderId: Long): List<POSOrderItemEntity>

    @Transaction
    suspend fun syncPOSData(orders: List<POSOrderEntity>, items: List<POSOrderItemEntity>) {
        // 1. Insert/Update Orders
        if (orders.isNotEmpty()) {
            insertOrders(orders)
        }
        // 2. Insert/Update Items
        if (items.isNotEmpty()) {
            insertOrderItems(items)
        }
    }
}