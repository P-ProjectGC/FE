package com.example.plango.data.login_api

import android.util.Log
import androidx.lifecycle.*
import com.example.plango.model.login_api.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    // ⚫ 일반 로그인 결과
    private val _normalLoginResult = MutableLiveData<Result<LoginData>?>()
    val normalLoginResult: LiveData<Result<LoginData>?> = _normalLoginResult

    // 🟡 카카오 로그인 결과
    private val _kakaoLoginState = MutableLiveData<Result<KakaoLoginData>?>()
    val kakaoLoginState: LiveData<Result<KakaoLoginData>?> get() = _kakaoLoginState

    // 🔵 (추후용) 토큰 재발급
    private val _tokenRefreshResult = MutableLiveData<Result<LoginData>>()
    val tokenRefreshResult: LiveData<Result<LoginData>> = _tokenRefreshResult
  
    // 로딩 화면
    private val _loading = MutableLiveData<Boolean>()

    val loading: LiveData<Boolean> get() = _loading

    /**
     * ------------------------
     * 🔐 일반 로그인
     * ------------------------
     */
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


    /**
     * ------------------------
     * 🟡 카카오 로그인
     * ------------------------
     *
     * repository.loginKakao() 의 반환 타입:
     * → Response<KakaoLoginResponse>
     *
     * KakaoLoginResponse.data 가 실제 유저 정보
     */
    fun loginKakao(accessToken: String, idToken: String?) {

        viewModelScope.launch {
            try {
                val request = KakaoLoginRequest(
                    accessToken = accessToken,
                    idToken = idToken ?: ""   // nullable 대응
                )

                val response = repository.loginKakao(request)

                Log.d("KAKAO_FLOW", "2️⃣ 서버 응답 코드 = ${response.code()} | body = ${response.body()}")

                if (response.isSuccessful) {
                    val body = response.body()!!
                    _kakaoLoginState.postValue(Result.success(body.data))
                } else {
                    _kakaoLoginState.postValue(Result.failure(Exception("카카오 로그인 실패")))
                }

            } catch (e: Exception) {
                _kakaoLoginState.postValue(Result.failure(e))
            }
        }
    }

    fun clearState() {
        _normalLoginResult.value = null
        _kakaoLoginState.value = null
    }

    /**
     * ------------------------
     * 🔄 토큰 재발급 (추후 기능)
     * ------------------------
     */
//    fun refreshToken(refreshToken: String) {
//        viewModelScope.launch {
//            _tokenRefreshResult.value = repository.refreshToken(refreshToken)
//        }
//    }
}