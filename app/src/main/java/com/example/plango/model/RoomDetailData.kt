package com.example.plango.model


data class RoomDetailData(
    val roomId: Long,
    val roomName: String,
    val memo: String?,
    val startDate: String,
    val endDate: String,
    val host: Boolean,  // ✅ 현재 로그인한 사용자가 방장인지 여부
    val members: List<RoomMemberDetail>
)