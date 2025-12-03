package com.example.plango.data.login_api

import androidx.lifecycle.*
import com.example.plango.model.login_api.LoginData
import com.example.plango.model.login_api.LoginRequest
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginData>>()
    val loginResult: LiveData<Result<LoginData>> = _loginResult

    // 일반 로그인 실행
    fun loginNormal(loginId: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = repository.loginNormal(
                LoginRequest(loginId, password)
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(private val repository: AuthRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository) as T
    }
}