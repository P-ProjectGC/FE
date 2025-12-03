package com.example.plango.model

// POST /api/friends 요청 본문
data class FriendRequest(
    val targetNickname: String // 친구로 추가할 상대 닉네임
)