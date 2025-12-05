package com.example.plango.data.login_api

import com.example.plango.model.login_api.KakaoLoginRequest
import com.example.plango.model.ApiResponse
import com.example.plango.model.login_api.NicknameCheckResponse
import com.example.plango.model.login_api.KakaoLoginResponse
import com.example.plango.model.login_api.LoginRequest
import com.example.plango.model.login_api.LoginResponse
import com.example.plango.model.login_api.RefreshTokenRequest
import com.example.plango.model.login_api.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
    ): Response<KakaoLoginResponse>

    // 토큰 재발급 API
    @POST("/api/auth/token/reissue")
    suspend fun reissueToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    // 🔹 닉네임 중복확인
    @GET("/api/auth/check/nickname")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<NicknameCheckResponse>

    // ✅ 로그아웃
    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>
}