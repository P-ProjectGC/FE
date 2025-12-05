package com.example.plango.model.signup_api

data class SignupResponse(
    val code: Int,
    val message: String,
    val data: SignupData?
)

data class SignupData(
    val memberId: Int,
    val name: String,
    val nickname: String,
    val loginId: String,
    val email: String
)