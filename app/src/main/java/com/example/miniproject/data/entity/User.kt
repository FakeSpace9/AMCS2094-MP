package com.example.miniproject.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val role: String,
    val uid: String,
    val isLoggedIn: Boolean
)
