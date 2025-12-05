package com.example.plango.model

data class RoomDetailData(
    val roomId: Long,
    val roomName: String,
    val memo: String?,
    val startDate: String,  // 서버 응답: "MM:dd"
    val endDate: String,    // 서버 응답: "MM:dd"
    val host:  Boolean,         // 방장 memberId
    val members: List<RoomMemberDeatail>
)
