package com.example.plango.data.token

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("token_prefs", Context.MODE_PRIVATE)

    // 🔹 AccessToken 저장
    fun saveAccessToken(token: String) {
        prefs.edit()
            .putString("access_token", token)
            .apply()
    }

    // 🔹 RefreshToken 저장
    fun saveRefreshToken(token: String) {
        prefs.edit()
            .putString("refresh_token", token)
            .apply()
    }

    // 한번에 두 토큰 저장하는 함수
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    // 🔹 AccessToken 조회
    fun getAccessToken(): String? =
        prefs.getString("access_token", null)

    // 🔹 RefreshToken 조회
    fun getRefreshToken(): String? =
        prefs.getString("refresh_token", null)

    // 🔹 전체 삭제 (로그아웃 시)
    fun clearTokens() {
        prefs.edit()
            .clear()
            .apply()
    }
}