package com.example.miniproject.repository

import com.example.miniproject.data.dao.PaymentDao
import com.example.miniproject.data.entity.PaymentEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun getPayments(customerId: String): List<PaymentEntity> {
        return paymentDao.getPayments(customerId)
    }

    suspend fun getPaymentById(id: Long): PaymentEntity? {
        return paymentDao.getPaymentById(id)
    }

    suspend fun savePayment(payment: PaymentEntity): Result<Long> {
        return try {
            val id = paymentDao.insertPayment(payment)

            val map = hashMapOf(
                "paymentType" to payment.paymentType,
                "displayName" to payment.displayName,
                "cardHolderName" to payment.cardHolderName,
                "cardNumber" to payment.cardNumber,
                "expiryMonth" to payment.expiryMonth,
                "expiryYear" to payment.expiryYear,
                "walletId" to payment.walletId,
                "isDefault" to payment.isDefault
            )

            firestore.collection("customers")
                .document(payment.customerId)
                .collection("payments")
                .document(id.toString())
                .set(map)
                .await()

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePayment(customerId: String, id: Long): Result<Unit> {
        return try {
            paymentDao.deletePayment(id)

            firestore.collection("customers")
                .document(customerId)
                .collection("payments")
                .document(id.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefault(customerId: String, id: Long) {
        paymentDao.clearDefault(customerId)
        paymentDao.setDefault(id)

        firestore.collection("customers")
            .document(customerId)
            .collection("payments")
            .document(id.toString())
            .update("isDefault", true)
    }
}
