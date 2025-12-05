package com.example.plango.model

data class FriendRequestResponse(
    val friendId: Long,
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val createdAt: String,
    val loginType: String?      // 🔥 nullable 로 해두는 게 안전
)