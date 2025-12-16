package com.example.miniproject.data.dao

import androidx.room.*
import com.example.miniproject.data.entity.AddressEntity
import com.example.miniproject.data.entity.PaymentEntity
import com.example.miniproject.data.entity.ProductEntity
import com.example.miniproject.data.entity.ProductVariantEntity

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payment WHERE customerId = :customerId")
    suspend fun getPayments(customerId: String): List<PaymentEntity>

    @Query("SELECT * FROM payment WHERE paymentId = :id")
    suspend fun getPaymentById(id: Long): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("DELETE FROM payment WHERE paymentId = :id")
    suspend fun deletePayment(id: Long)

    @Query("DELETE FROM payment WHERE customerId = :customerId")
    suspend fun deletePaymentByCustomerId(customerId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayment(payment: List<PaymentEntity>)


}
