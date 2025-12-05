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

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading


    fun loginNormal(email: String, password: String) {
        viewModelScope.launch {

            _loading.value = true  // 🔥 로딩 시작

            try {
                val result = repository.loginNormal(
                    LoginRequest(email, password)
                )
                _normalLoginResult.value = result

            } finally {
                _loading.value = false // 🔥 로딩 끝
            }
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