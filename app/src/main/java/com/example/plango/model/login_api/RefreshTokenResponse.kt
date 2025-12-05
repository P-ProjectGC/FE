package com.example.plango.model.login_api

data class RefreshTokenResponse(
    val code: Int,
    val message: String,
    val data: LoginData?
)