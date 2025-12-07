package com.example.plango.model.findid

data class SendFindIdCodeData(
    val email: String,
    val maskedEmail: String,
    val verificationCode: String // 개발용
)
