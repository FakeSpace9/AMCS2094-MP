


import com.example.miniproject.data.dao.AdminDao
import com.example.miniproject.data.entity.AdminEntity
import com.example.miniproject.data.entity.CustomerEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class LoginRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val customerDao: CustomerDao,
    private val adminDao: AdminDao
) {


    suspend fun logoutFirebase() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore logout errors
        }
    }

    suspend fun login(email: String, password: String): Result<CustomerEntity> {
        return try {
            // 1. Firebase Auth login
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User not found"))

            val uid = user.uid

            // 2. Fetch Firestore user data using UID
            val snapshot = firestore.collection("customers")
                .document(uid)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("User data not found in Firestore"))
            }


            // Convert Firestore -> local Room entity
            val customerEntity = CustomerEntity(
                customerId = uid,
                email = snapshot.getString("email") ?: "",
                name = snapshot.getString("name") ?: "",
                phone = snapshot.getString("phone") ?: "",
            )

            // 3. Save to Room local database
            customerDao.insertCustomer(customerEntity)

            Result.success(customerEntity)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun adminLogin(email: String, password: String): Result<AdminEntity> {
        return try {
            // 1. Firebase Auth login
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User not found"))

            val uid = user.uid

            // 2. Fetch Firestore user data using UID
            val snapshot = firestore.collection("admins")
                .document(uid)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("User data not found in Firestore"))
            }


            // Convert Firestore -> local Room entity
            val adminEntity = AdminEntity(
                adminId = uid,
                email = snapshot.getString("email") ?: "",
                name = snapshot.getString("name") ?: "",
                phone = snapshot.getString("phone") ?: "",
            )

            // 3. Save to Room local database
            adminDao.insertAdmin(adminEntity)


            Result.success(adminEntity)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<CustomerEntity> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            val result = auth.signInWithCredential(credential).await()
            val firebaseCust = result.user ?: return Result.failure(Exception("Firebase user not found"))
            val uid = firebaseCust.uid

            // 1. check Firestore
            val docRef = firestore.collection("customers").document(uid)
            val snapshot = docRef.get().await()

            val customerEntity = if (!snapshot.exists()) {
                // 2. create new Firestore user
                val newUser = mapOf(
                    "customerId" to uid,
                    "email" to (firebaseCust.email ?: ""),
                    "name" to (firebaseCust.displayName ?: ""),
                    "phone" to (firebaseCust.phoneNumber ?: "")
                )
                docRef.set(newUser).await()

                CustomerEntity(
                    customerId = uid,
                    email = firebaseCust.email ?: "",
                    name = firebaseCust.displayName ?: "",
                    phone = firebaseCust.phoneNumber?:""
                )
            } else {
                // existing user
                CustomerEntity(
                    customerId = uid,
                    email = snapshot.getString("email") ?: "",
                    name = snapshot.getString("name") ?: "",
                    phone = snapshot.getString("phone") ?: ""
                )
            }



            Result.success(customerEntity)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}


