package com.example.plango.model

data class TravelRoom(
    val id: Long,         // 나중에 room_id 같은 PK 들어갈 자리
    val title: String,    // 방 이름 (예: "여름 부산 여행")
    val dateText: String, // 날짜 텍스트 (예: "10월 28일 - 10월 30일")
    val memo: String,     // 한 줄 메모 (예: "해운대 방문 예정")
    val memberCount: Int,  // 인원 수 (예: 4)
    val startDate: String,
    val endDate: String,
)
