package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.miniproject.data.entity.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(users: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?


}