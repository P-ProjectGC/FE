package com.example.plango.model.login_api

data class NicknameCheckResponse(
    val code: Int,
    val message: String,
    val data: NicknameCheckData
)

data class NicknameCheckData(
    val available: Boolean,
    val field: String,
    val value: String
)