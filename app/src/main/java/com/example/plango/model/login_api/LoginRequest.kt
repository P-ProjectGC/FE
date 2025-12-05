package com.example.plango.model.login_api

// 로그인 요청 바디
data class LoginRequest(
    val loginId: String,   // 아이디
    val password: String   // 비밀번호
)

data class KakaoLoginRequest(
    val authorizationCode: String
)
