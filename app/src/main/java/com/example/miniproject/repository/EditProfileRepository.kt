package com.example.miniproject.repository

import com.example.miniproject.data.dao.CustomerDao
import com.example.miniproject.data.entity.CustomerEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EditProfileRepository(
    private val customerDao: CustomerDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun getCustomerByEmail(email: String): CustomerEntity {
        return customerDao.getCustomerByEmail(email)
    }

    suspend fun getCustomerById(customerId: String): CustomerEntity {
        return customerDao.getCustomerById(customerId)
    }

    suspend fun updateProfile(customer: CustomerEntity): Result<Unit> {
        return try {
            // 1. Update Room
            customerDao.updateCustomer(customer)

            // 2. Update Firestore (by email)
            val data = mapOf(
                "name" to customer.name,
                "phone" to customer.phone,
                "email" to customer.email
            )

            firestore.collection("customers")
                .whereEqualTo("email", customer.email)
                .get()
                .await()
                .documents
                .first()
                .reference
                .update(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}