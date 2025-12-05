package com.example.plango.data.login_api

import androidx.lifecycle.*
import com.example.plango.model.login_api.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    // ⚫ 일반 로그인 결과
    private val _normalLoginResult = MutableLiveData<Result<LoginData>>()
    val normalLoginResult: LiveData<Result<LoginData>> = _normalLoginResult

    // 🟡 카카오 로그인 결과
    private val _kakaoLoginState = MutableLiveData<Result<KakaoLoginData>>()
    val kakaoLoginState: LiveData<Result<KakaoLoginData>> get() = _kakaoLoginState

    // 🔵 (추후용) 토큰 재발급
    private val _tokenRefreshResult = MutableLiveData<Result<LoginData>>()
    val tokenRefreshResult: LiveData<Result<LoginData>> = _tokenRefreshResult

    // 닉네임 중복 확인
    private val _nicknameCheckState = MutableLiveData<Result<Boolean>>()
    val nicknameCheckState: LiveData<Result<Boolean>> = _nicknameCheckState


    /**
     * ------------------------
     * 🔐 일반 로그인
     * ------------------------
     */
    fun loginNormal(email: String, password: String) {
        viewModelScope.launch {
            _normalLoginResult.value = repository.loginNormal(
                LoginRequest(email, password)
            )
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
    fun loginKakao(accessToken: String, idToken: String) {
        viewModelScope.launch {
            val request = KakaoLoginRequest(accessToken, idToken)

            // Repository에서 이미 Result<KakaoLoginData> 형태로 준다!
            val result = repository.loginKakao(request)

            _kakaoLoginState.value = result
        }
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

    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameCheckState.value = repository.checkNickname(nickname)
        }
    }
}