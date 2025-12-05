package com.example.plango.data.signup_api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plango.model.signup_api.SignupRequest
import com.example.plango.model.signup_api.SignupResponse
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: SignupRepository) : ViewModel() {

    private val _signupResult = MutableLiveData<SignupResponse?>()
    val signupResult: LiveData<SignupResponse?> get() = _signupResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    // 닉네임 중복 확인
    private val _nicknameCheckState = MutableLiveData<Result<Boolean>>()
    val nicknameCheckState: LiveData<Result<Boolean>> = _nicknameCheckState

    // id 중복 확인
    private val _loginIdCheckState = MutableLiveData<Result<Boolean>>()
    val loginIdCheckState: LiveData<Result<Boolean>> = _loginIdCheckState

    // email 중복 확인
    private val _emailCheckState = MutableLiveData<Result<Boolean>>()
    val emailCheckState: LiveData<Result<Boolean>> = _emailCheckState


    fun signup(name: String, nickname: String, loginId: String, password: String, email: String) {
        viewModelScope.launch {
            try {

                _loading.value = true   // 🔥 로딩 시작

                val request = SignupRequest(name, nickname, loginId, password, email)
                val response = repository.signup(request)

                Log.d("SIGNUP_DEBUG", "isSuccessful = ${response.isSuccessful}")
                Log.d("SIGNUP_DEBUG", "code = ${response.code()}")
                Log.d("SIGNUP_DEBUG", "errorBody = ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    _signupResult.value = response.body()
                } else {
                    _signupResult.value = null
                }

            } catch (e: Exception) {
                _signupResult.value = null
            } finally {
                _loading.value = false  // 🔥 로딩 끝
            }
        }
    }

    // 닉네임 중복 확인
    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameCheckState.value = repository.checkNickname(nickname)
        }
    }

    // id 중복 확인
    fun checkLoginId(loginId: String) {
        viewModelScope.launch {
            _loginIdCheckState.value = repository.checkLoginId(loginId)
        }
    }

    // email 중복 확인
    fun checkEmail(email: String) {
        viewModelScope.launch {
            _emailCheckState.value = repository.checkEmail(email)
        }
    }
}