package com.example.plango.model


data class TravelScheduleItem(
    // 🚨 [필수 추가] 일정 고유 ID (수정/삭제 API 호출에 필요)
    val scheduleId: Long,

    // 🚨 [필수 추가] 메모 (수정 API 호출 시 기존 값을 전달하기 위해 필요)
    val memo: String?,
    val roomPlaceId: Long, // 🚨 [추가]: 서버 위시리스트 ID (장소의 근원 ID)

    val timeLabel: String,   // 왼쪽에 표시할 시간 (예: "10:00")
    val placeName: String,   // 장소 이름 (예: 해운대 해수욕장)
    val timeRange: String,   // 시간 범위 (예: 10:00 ~ 13:00)
    val address: String,     // 주소 (예: 부산 해운대구)
    val lat: Double,         // 위도
    val lng: Double          // 경도
)