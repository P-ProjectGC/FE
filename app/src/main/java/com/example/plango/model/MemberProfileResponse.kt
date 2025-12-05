package com.example.plango.model

data class MemberProfileResponse(
    val code: Int,
    val message: String,
    val data: MemberProfileData?
)
