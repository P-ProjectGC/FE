package com.example.plango.data.login_api

import com.example.plango.model.ApiResponse
import com.example.plango.model.NicknameCheckResponse
import com.example.plango.model.login_api.LoginRequest
import com.example.plango.model.login_api.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {

    @POST("/api/auth/login")  // 일반 로그인 API
    suspend fun loginNormal(
        @Body request: LoginRequest
    ): Response<LoginResponse>


    // 🔹 닉네임 중복확인
    @GET("/api/auth/check/nickname")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<NicknameCheckResponse>

    // ✅ 로그아웃
    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>
}