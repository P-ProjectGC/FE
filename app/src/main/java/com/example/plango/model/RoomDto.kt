package com.example.plango.model


// RoomMemberDto: 상세조회에서 내려오는 members 배열
data class RoomMemberDto(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val host: Boolean          // 이 멤버가 방장인지?
)

// RoomDto: 목록/상세 둘 다 커버 (members는 목록에서는 null일 수 있음)
data class RoomDto(
    val roomId: Long,
    val roomName: String,
    val memo: String?,
    val startDate: String,
    val endDate: String,

    // 이 요청자(X-MEMBER-ID)가 방장인지?
    val host: Boolean? = null,

    // 상세 조회에서만 채워질 수 있는 멤버 목록
    val members: List<RoomMemberDto>? = null
)
