package com.example.plango.data.login_api

import com.example.plango.model.login_api.KakaoLoginRequest
import com.example.plango.model.login_api.LoginData
import com.example.plango.model.login_api.LoginRequest
import com.example.plango.model.login_api.RefreshTokenRequest

class AuthRepository(
    private val service: AuthService
) {

    // 일반 로그인
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


    // 카카오 로그인
    suspend fun loginKakao(authorizationCode: String): Result<LoginData> = try {

        val req = KakaoLoginRequest(authorizationCode)
        val response = service.loginKakao(req)

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


    // 토큰 재발급
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