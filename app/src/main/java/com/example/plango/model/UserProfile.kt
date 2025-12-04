package com.example.plango.model

data class UserProfile(
    val memberId: Long,
    val email: String,
    val nickname: String,
    val loginId: String?,
    val name: String?,
    val profileImageUrl: String?,
    val loginType: String?
)
