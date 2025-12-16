package com.example.miniproject.repository

import com.example.miniproject.data.dao.PaymentDao
import com.example.miniproject.data.entity.AddressEntity
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
                "paymentId" to id,
                "customerId" to payment.customerId,
                "paymentType" to payment.paymentType,
                "displayName" to payment.displayName,
                "cardHolderName" to payment.cardHolderName,
                "cardNumber" to payment.cardNumber,
                "expiryMonth" to payment.expiryMonth,
                "expiryYear" to payment.expiryYear,
                "cvv" to payment.cvv,
                "walletId" to payment.walletId,

            )

            firestore.collection("payments")
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

            firestore.collection("payments")
                .document(id.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentsFromFirebase(customerId: String) {
        try {
            val snapshot = firestore.collection("payments")
                .whereEqualTo("customerId", customerId)
                .get()
                .await()

            val payments = snapshot.documents.mapNotNull {
                it.toObject(PaymentEntity::class.java)
            }

            paymentDao.deletePaymentByCustomerId(customerId)

            paymentDao.insertAllPayment(payments)
        } catch (e: Exception) {
        }
    }
}
