package com.example.plango.model

data class NicknameCheckResponse(
    val code: Int,
    val message: String,
    val data: NicknameCheckData?
)