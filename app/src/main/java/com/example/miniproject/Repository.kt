package com.example.miniproject

import com.example.miniproject.data.dao.UserDao
import com.example.miniproject.data.entity.User

class Repository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }


}