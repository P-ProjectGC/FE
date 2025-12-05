package com.example.plango.model

data class RoomMemberDeatail(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val host: Boolean   // 이 멤버가 방장인지 여부
)
