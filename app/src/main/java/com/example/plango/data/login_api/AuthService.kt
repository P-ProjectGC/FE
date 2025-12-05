package com.example.plango.data.login_api

import com.example.plango.model.login_api.KakaoLoginRequest
import com.example.plango.model.login_api.LoginRequest
import com.example.plango.model.login_api.LoginResponse
import com.example.plango.model.login_api.RefreshTokenRequest
import com.example.plango.model.login_api.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    // 일반 로그인 API
    @POST("/api/auth/login")
    suspend fun loginNormal(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // 카카오 로그인 API
    @POST("/api/auth/login/kakao")
    suspend fun loginKakao(
        @Body request: KakaoLoginRequest
    ): Response<LoginResponse>

    // 토큰 재발급 API
    @POST("/api/auth/token/reissue")
    suspend fun reissueToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

}