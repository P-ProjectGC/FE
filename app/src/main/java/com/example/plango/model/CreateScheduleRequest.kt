package com.example.plango.model

data class CreateScheduleRequest(
    val roomPlaceId: Long?,   // 일정이 등록될 여행방 장소 ID
    val dayIndex: Int,       // 여행 n일차 인덱스 (현재 0부터 시작)
    val startTime: String,   // "HH:mm"
    val endTime: String,     // "HH:mm"
    val memo: String? = null // 지금은 안 쓰지만 서버 스펙에 있으니 nullable로 둠
)
