package com.example.plango.model.signup_api

data class SignupRequest(
    val name: String,
    val nickname: String,
    val loginId: String,
    val password: String,
    val email: String
)