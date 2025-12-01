package com.example.plango.model

data class CreateRoomRequest(
    val roomName: String,
    val memo: String?,          // 메모는 선택
    val startDate: String,      // "2025-12-24" 형식
    val endDate: String,        // "2025-12-26"
    val memberIds: List<Long>   // [1, 2, 3]
)
