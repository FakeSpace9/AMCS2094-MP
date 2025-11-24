import com.example.miniproject.data.dao.UserDao
import com.example.miniproject.data.entity.UserEntity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await




class LoginRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            // 1. Firebase Auth login
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User not found"))

            val uid = user.uid

            // 2. Fetch Firestore user data using UID
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("User data not found in Firestore"))
            }

            // Convert Firestore -> local Room entity
            val userEntity = UserEntity(
                uid = uid,
                email = snapshot.getString("email") ?: "",
                name = snapshot.getString("name") ?: "",
                role = snapshot.getString("role") ?: "customer"
            )

            // 3. Save to Room local database
            userDao.insertUser(userEntity)

            Result.success(userEntity)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
