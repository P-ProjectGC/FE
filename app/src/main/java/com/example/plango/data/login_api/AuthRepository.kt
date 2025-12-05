package com.example.plango.data.login_api

import com.example.plango.model.login_api.*
import retrofit2.Response

class AuthRepository(
    private val service: AuthService
) {

    /**
     * ------------------------------------
     * 🔐 일반 로그인
     * ------------------------------------
     */
    suspend fun loginNormal(req: LoginRequest): Result<LoginData> = try {
        val response = service.loginNormal(req)

        if (response.isSuccessful) {
            val body = response.body()

            when {
                body == null -> Result.failure(Exception("Response body is null"))
                body.data == null -> Result.failure(Exception(body.message))
                else -> Result.success(body.data)
            }

        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }

    } catch (e: Exception) {
        Result.failure(e)
    }


    /**
     * ------------------------------------
     * 🟡 카카오 로그인
     * ------------------------------------
     * Response<KakaoLoginResponse> 를 그대로 ViewModel로 넘기지 않고
     * 여기서 data만 추출해주는 방식으로 통일하는 것이 중요!
     */
    suspend fun loginKakao(request: KakaoLoginRequest): Result<KakaoLoginData> = try {

        val response = service.loginKakao(request)

        if (response.isSuccessful) {
            val body = response.body()

            when {
                body == null -> Result.failure(Exception("Response body is null"))
                body.data == null -> Result.failure(Exception(body.message))
                else -> Result.success(body.data)
            }

        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }

    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * ------------------------------------
     * 🔄 토큰 재발급 (추후 기능)
     * ------------------------------------
     */
//    suspend fun refreshToken(refreshToken: String): Result<LoginData> = try {
//
//        val request = RefreshTokenRequest(refreshToken)
//        val response = service.reissueToken(request)
//
//        if (response.isSuccessful) {
//            val body = response.body()
//
//            when {
//                body == null -> Result.failure(Exception("Response body is null"))
//                body.data == null -> Result.failure(Exception(body.message))
//                else -> Result.success(body.data)
//            }
//
//        } else {
//            Result.failure(Exception("HTTP ${response.code()}"))
//        }
//
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
}