package com.example.plango.data.token

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val token = tokenManager.getAccessToken()

        val newRequest = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                // Authorization 헤더 자동 추가
                addHeader("Authorization", "Bearer $token")
                // TODO: 토큰 테스트 코드
                Log.d("TOKEN_TEST", "요청에 자동 추가된 Authorization = Bearer $token")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}