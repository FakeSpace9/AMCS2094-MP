import com.example.miniproject.data.dao.UserDao
import com.example.miniproject.data.entity.UserEntity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.GoogleAuthProvider




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

            userDao.logoutAllUsers()
            // Convert Firestore -> local Room entity
            val userEntity = UserEntity(
                uid = uid,
                email = snapshot.getString("email") ?: "",
                name = snapshot.getString("name") ?: "",
                role = snapshot.getString("role") ?: "",
                isLoggedIn = true
            )

            // 3. Save to Room local database
            userDao.insertUser(userEntity)

            Result.success(userEntity)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(){
        auth.signOut()
        userDao.logoutAllUsers()
    }
    suspend fun loginWithGoogle(idToken: String): Result<UserEntity> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Firebase user not found"))
            val uid = firebaseUser.uid

            // 1. check Firestore
            val docRef = firestore.collection("users").document(uid)
            val snapshot = docRef.get().await()

            val userEntity = if (!snapshot.exists()) {
                // 2. create new Firestore user
                val newUser = mapOf(
                    "uid" to uid,
                    "email" to (firebaseUser.email ?: ""),
                    "name" to (firebaseUser.displayName ?: ""),
                    "role" to "customer"
                )
                docRef.set(newUser).await()

                UserEntity(
                    uid = uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "",
                    role = "customer",
                    isLoggedIn = true
                )
            } else {
                // existing user
                UserEntity(
                    uid = uid,
                    email = snapshot.getString("email") ?: "",
                    name = snapshot.getString("name") ?: "",
                    role = snapshot.getString("role") ?: "customer",
                    isLoggedIn = true
                )
            }

            // clear previous users (local login)
            userDao.logoutAllUsers()
            userDao.insertUser(userEntity)

            Result.success(userEntity)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}


