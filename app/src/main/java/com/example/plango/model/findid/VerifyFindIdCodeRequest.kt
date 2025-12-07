package com.example.plango.model.findid

data class VerifyFindIdCodeRequest(
    val email: String,
    val code: String
)