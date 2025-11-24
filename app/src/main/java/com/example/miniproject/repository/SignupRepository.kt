package com.example.miniproject.data

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
        email: String, password: String, name: String
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.success(null)

            val userData = mapOf(
                "email" to email, "name" to name, "role" to "customer"
            )

            firestore.collection("users").document(user.uid).set(userData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSignup(
        email: String, password: String, name: String
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.success(null)

            val userData = mapOf(
                "email" to email, "name" to name, "role" to "admin"
            )

            firestore.collection("users").document(user.uid).set(userData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
