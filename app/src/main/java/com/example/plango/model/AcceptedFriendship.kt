package com.example.plango.model

/**
 * 친구 요청 수락 성공 시 서버가 반환하는 'data' 필드 구조
 */
data class AcceptedFriendship(
    val friendshipId: Long, // 새로 생성된 친구 관계 ID
    val status: String = "FRIENDS"
    // 기타 필요한 필드 (예: acceptedAt)
)