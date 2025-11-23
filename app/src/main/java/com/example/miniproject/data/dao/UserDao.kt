package com.example.miniproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.miniproject.data.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(users: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

}