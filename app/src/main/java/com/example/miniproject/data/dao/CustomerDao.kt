
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.miniproject.data.entity.CustomerEntity

@Dao
interface CustomerDao {

    // 1. Register a new Customer
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    // 2. Login Check
    @Query("SELECT * FROM customers WHERE email = :email")
    suspend fun login(email: String): CustomerEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM customers WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean
}