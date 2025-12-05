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
}