package com.example.plango.model.login_api

// API 응답 전체
data class LoginResponse(
    val code: String,        // 응답 코드
    val message: String,    // 응답 메시지
    val data: LoginData?     // 로그인 결과 정보
)

// 로그인 성공 시 data 내부 구조
data class LoginData(
    val memberId: Int,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?,
    val newMember: Boolean,
    val accessToken: String,
    val refreshToken: String
)

data class KakaoLoginResponse(
    val code: Int,
    val message: String,
    val data: KakaoLoginData
)

data class KakaoLoginData(
    val memberId: Int,
    val email: String,
    val nickname: String,
    val profileImageUrl: String,
    val newMember: Boolean,
    val accessToken: String,
    val refreshToken: String
)
