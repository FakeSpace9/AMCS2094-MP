package com.example.miniproject.repository

import android.net.Uri
import com.example.miniproject.data.dao.CustomerDao
import com.example.miniproject.data.entity.CustomerEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EditProfileRepository(
    private val customerDao: CustomerDao,
    private val firestore: FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth
) {

    private val storage = FirebaseStorage.getInstance()
    suspend fun getCustomerByEmail(email: String): CustomerEntity {
        return customerDao.getCustomerByEmail(email)
    }

    suspend fun getCustomerById(customerId: String): CustomerEntity? {
        return customerDao.getCustomerById(customerId)
    }

    suspend fun uploadProfilePicture(uri: Uri): Result<String> {
        return try {
            val filename = UUID.randomUUID().toString()
            val ref = storage.reference.child("profile_images/$filename")

            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateProfile(customer: CustomerEntity): Result<Unit> {
        return try {
            // 1. Update Room
            customerDao.updateCustomer(customer)

            // 2. Update Firestore (by email)
            val data = mapOf(
                "name" to customer.name,
                "phone" to customer.phone,
                "email" to customer.email,
                "profilePictureUrl" to customer.profilePictureUrl
            )

            firestore.collection("customers")
                .whereEqualTo("email", customer.email)
                .get()
                .await()
                .documents
                .first()
                .reference
                .update(data as Map<String, Any>)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPass: String, newPass: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPass)
            user.reauthenticate(credential).await()
            user.updatePassword(newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}