package com.example.plango.model

// TravelDailySchedule.kt (새 파일 or 기존 모델 파일 밑에 추가)


data class TravelDailySchedule(
    val dayIndex: Int,          // 0부터 시작 (0: 1일차, 1: 2일차 ...)
    val dayTitle: String,       // "1일차", "2일차" 같은 표시용 문자열
    val items: List<TravelScheduleItem>
)
