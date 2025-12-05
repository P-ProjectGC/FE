package com.example.plango.data.token

import android.util.Log
import com.example.plango.data.login_api.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val accessToken = tokenManager.getAccessToken()

        val request = chain.request().newBuilder().apply {
            if (!accessToken.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $accessToken")
                Log.d("TOKEN_TEST", "Authorization = Bearer $accessToken")
            }
        }.build()

        return chain.proceed(request)
    }
}
