package com.example.miniproject.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SignupRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // Function to create a user with email and password
    suspend fun signup(
        email: String, password: String, name: String, phone: String
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.success(null)

            val custData = mapOf(
                "customerId" to user.uid,"email" to email, "name" to name, "phone" to phone
            )

            firestore.collection("customers").document(user.uid).set(custData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSignup(
        email: String, password: String, name: String, phone:String
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.success(null)

            val adminData = mapOf(
                "adminId" to user.uid,"email" to email, "name" to name, "phone" to phone
            )

            firestore.collection("admins").document(user.uid).set(adminData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
