package com.example.plango.data.signup_api

import com.example.plango.model.signup_api.SignupRequest
import com.example.plango.model.signup_api.SignupResponse
import retrofit2.Response

class SignupRepository(private val service: SignupService) {

    // 회원가입
    suspend fun signup(request: SignupRequest): Response<SignupResponse> {
        return service.signup(request)
    }

    // 닉네임 중복 확인
    suspend fun checkNickname(nickname: String): Result<Boolean> = try {
        val response = service.checkNickname(nickname)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body.data.available)
            } else {
                Result.failure(Exception("Response body is null"))
            }
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 아이디 중복 확인
    suspend fun checkLoginId(loginId: String): Result<Boolean> = try {
        val response = service.checkLoginId(loginId)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body.data.available)
            } else {
                Result.failure(Exception("Response body is null"))
            }
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }


    // 이메일 중복 확인
    suspend fun checkEmail(email: String): Result<Boolean> = try {
        val response = service.checkEmail(email)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body.data.available)
            } else {
                Result.failure(Exception("Response body is null"))
            }
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}