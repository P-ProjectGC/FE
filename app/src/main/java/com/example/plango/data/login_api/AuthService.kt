package com.example.plango.data.login_api

import com.example.plango.model.login_api.KakaoLoginRequest
import com.example.plango.model.ApiResponse
import com.example.plango.model.findid.FindIdRequest
import com.example.plango.model.findid.FindIdResultData
import com.example.plango.model.findid.SendFindIdCodeData
import com.example.plango.model.findid.SendFindIdCodeRequest
import com.example.plango.model.findid.VerifyFindIdCodeData
import com.example.plango.model.findid.VerifyFindIdCodeRequest
import com.example.plango.model.findpassword.CheckLoginIdRequest
import com.example.plango.model.findpassword.ResetPasswordRequest
import com.example.plango.model.signup_api.NicknameCheckResponse
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

    // ✅ 로그아웃
    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>


    // 🔹 아이디 찾기 관련 3개 API
    // ============================

    // 1) 이메일로 마스킹된 로그인 아이디 조회
    @POST("/api/auth/find-id")
    suspend fun findId(
        @Body request: FindIdRequest
    ): Response<ApiResponse<FindIdResultData>>

    // 2) 인증번호 발송
    @POST("/api/auth/find-id/send-code")
    suspend fun sendFindIdCode(
        @Body request: SendFindIdCodeRequest
    ): Response<ApiResponse<SendFindIdCodeData>>

    // 3) 인증번호 검증 후 실제 로그인 아이디 반환
    @POST("/api/auth/find-id/verify-code")
    suspend fun verifyFindIdCode(
        @Body request: VerifyFindIdCodeRequest
    ): Response<ApiResponse<VerifyFindIdCodeData>>

    // 🔹 비밀번호 찾기 1단계: 아이디 검증
    @POST("/api/auth/find-password/check-login-id")
    suspend fun checkLoginIdForPassword(
        @Body request: CheckLoginIdRequest
    ): Response<ApiResponse<Any>>

    // 🔹 비밀번호 찾기 2단계: 새 비밀번호 설정
    @POST("/api/auth/find-password/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Any>>
}