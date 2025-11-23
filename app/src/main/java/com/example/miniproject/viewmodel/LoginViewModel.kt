package com.example.miniproject.viewmodel

import LoginRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = repository.login(email, password)
            if (result.isSuccess) {
                _loginState.value = LoginState.Success(result.getOrNull()!!)
            } else {
                _loginState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserEntity) : LoginState()
    data class Error(val message: String) : LoginState()
}
