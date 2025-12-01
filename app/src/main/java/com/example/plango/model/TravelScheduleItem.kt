package com.example.plango.model

data class TravelScheduleItem(
    val timeLabel: String,   // 왼쪽에 표시할 시간 (예: "10:00")
    val placeName: String,   // 장소 이름 (예: 해운대 해수욕장)
    val timeRange: String,   // 시간 범위 (예: 10:00 ~ 13:00)
    val address: String,     // 주소 (예: 부산 해운대구)
    val lat: Double,         // 위도
    val lng: Double          // 경도
)
