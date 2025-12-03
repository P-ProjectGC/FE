package com.example.plango.model

/**
 * 친구 요청 목록 (FriendRequestDialogFragment)에 표시될 데이터를 위한 모델.
 * 서버 통신 시 요청 ID(requestId)를 전달하기 위해 사용됩니다.
 */
// com.example.plango.model.FriendRequestItem
data class FriendRequestItem(
    val requestId: Long,        // 서버의 friendId
    val senderNickname: String,
    val senderMemberId: Long,
    val requestedAt: String,
    val isKakaoUser: Boolean    // 🔥 추가
)
