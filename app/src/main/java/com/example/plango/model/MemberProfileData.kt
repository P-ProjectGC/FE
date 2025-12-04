package com.example.plango.model

data class MemberProfileData(
    val memberId: Long,
    val name: String?,          // 🔹 이름(없으면 null로 들어오게)
    val nickname: String,
    val email: String,
    val loginId: String,
    val profileImageUrl: String?,
    val loginType: String?      // NORMAL / KAKAO / null
)
