package com.example.plango.data.login_api

import com.example.plango.model.login_api.LoginRequest
import com.example.plango.model.login_api.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/api/auth/login")  // 일반 로그인 API
    suspend fun loginNormal(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}