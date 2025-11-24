package com.example.miniproject.data

import android.content.Context

class AuthPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveLogin(email: String) {
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("email", email)
            putLong("loginTime", System.currentTimeMillis())
            apply()
        }
    }

    fun clearLogin() {
        prefs.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }
    }

    fun shouldAutoLogin(): Boolean {
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val lastTime = prefs.getLong("loginTime", 0)
        val sevenDays = 7 * 24 * 60 * 60 * 1000L

        return isLoggedIn && (System.currentTimeMillis() - lastTime <= sevenDays)
    }

    fun getLoggedInEmail(): String? {
        return prefs.getString("email", null)
    }
}
