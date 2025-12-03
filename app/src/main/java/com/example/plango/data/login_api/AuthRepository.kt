package com.example.plango.data.login_api

import com.example.plango.model.login_api.LoginData
import com.example.plango.model.login_api.LoginRequest

class AuthRepository(private val service: AuthService) {

    // 일반 로그인
    suspend fun loginNormal(req: LoginRequest): Result<LoginData> = try {

        val response = service.loginNormal(req)

        if (response.isSuccessful) {
            val body = response.body()

            when {
                body == null -> {
                    // body 자체가 null일 때
                    Result.failure(Exception("Response body is null"))
                }
                body.data == null -> {
                    // data가 null일 때
                    Result.failure(Exception(body.message))
                }
                else -> {
                    // 로그인 성공
                    Result.success(body.data)
                }
            }

        } else {
            // HTTP 오류
            Result.failure(Exception("HTTP ${response.code()}"))
        }

    } catch (e: Exception) {
        // 네트워크 오류
        Result.failure(e)
    }
}