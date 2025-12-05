package com.example.plango.model

data class SentFriendRequestItem(
    val friendId: Long,          // 서버에서 주는 friendId (요청 id 혹은 관계 id)
    val memberId: Long,          // 내가 친구 요청 보낸 대상의 memberId
    val nickname: String,        // 대상 닉네임
    val profileImageUrl: String?,// nullable 처리
    val createdAt: String        // "2025-12-03T17:58:56.770Z"
)
