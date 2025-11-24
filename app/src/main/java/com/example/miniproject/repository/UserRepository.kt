package com.example.miniproject.repository

import com.example.miniproject.data.dao.UserDao
import com.example.miniproject.data.entity.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getCurrentUser(): UserEntity? {
        return userDao.getCurrentUser()
    }

}