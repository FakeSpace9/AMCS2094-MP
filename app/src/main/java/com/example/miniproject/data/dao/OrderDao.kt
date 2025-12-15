package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.miniproject.data.entity.OrderEntity
import com.example.miniproject.data.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: List<OrderItemEntity>)

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity>

    @Query("""SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC""")
    suspend fun getOrdersByCustomer(customerId: String): List<OrderEntity>
}