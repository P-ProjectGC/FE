package com.example.plango.data.signup_api

import com.example.plango.model.signup_api.NicknameCheckResponse
import com.example.plango.model.signup_api.SignupRequest
import com.example.plango.model.signup_api.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SignupService {

    // 회원가입
    @POST("/api/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    // 닉네임 중복확인
    @GET("/api/auth/check/nickname")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<NicknameCheckResponse>

    // id 중복 확인
    @GET("/api/auth/check/login-id")
    suspend fun checkLoginId(
        @Query("loginId") loginId: String
    ): Response<NicknameCheckResponse>   // 동일 구조 재사용

    // email 중복 확인
    @GET("/api/auth/check/email")
    suspend fun checkEmail(
        @Query("email") email: String
    ): Response<NicknameCheckResponse>
}