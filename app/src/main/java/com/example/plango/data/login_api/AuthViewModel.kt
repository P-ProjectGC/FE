package com.example.plango.data.login_api

import androidx.lifecycle.*
import com.example.plango.model.login_api.LoginData
import com.example.plango.model.login_api.LoginRequest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _normalLoginResult = MutableLiveData<Result<LoginData>>()
    val normalLoginResult: LiveData<Result<LoginData>> = _normalLoginResult

    private val _kakaoLoginResult = MutableLiveData<Result<LoginData>>()
    val kakaoLoginResult: LiveData<Result<LoginData>> = _kakaoLoginResult

    private val _tokenRefreshResult = MutableLiveData<Result<LoginData>>()
    val tokenRefreshResult: LiveData<Result<LoginData>> = _tokenRefreshResult


    fun loginNormal(email: String, password: String) {
        viewModelScope.launch {
            _normalLoginResult.value = repository.loginNormal(
                LoginRequest(email, password)
            )
        }
    }

    fun loginKakao(authorizationCode: String) {
        viewModelScope.launch {
            _kakaoLoginResult.value = repository.loginKakao(authorizationCode)
        }
    }

//    fun refreshToken(refreshToken: String) {
//        viewModelScope.launch {
//            _tokenRefreshResult.value = repository.refreshToken(refreshToken)
//        }
//    }
}