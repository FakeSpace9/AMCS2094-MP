
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.SignupRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    data class Success(val user: FirebaseUser) : SignupState()
    data class Error(val message: String) : SignupState()
}

class SignupViewModel(private val repository: SignupRepository) : ViewModel() {

    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)
    val signupState: StateFlow<SignupState> = _signupState

    fun signup(email: String, password: String,name: String,phone:String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading
            val result = repository.signup(email, password,name, phone)
            _signupState.value = if (result.isSuccess && result.getOrNull() != null) {
                SignupState.Success(result.getOrNull()!!)
            } else {
                SignupState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun adminSignup(email: String, password: String,name: String,phone:String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading
            val result = repository.adminSignup(email, password,name,phone)
            _signupState.value = if (result.isSuccess && result.getOrNull() != null) {
                SignupState.Success(result.getOrNull()!!)
            } else {
                SignupState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }
    fun clearMessages() {
        _signupState.value = SignupState.Idle
    }

}