package com.example.plango.model


import java.time.LocalDate

// 날짜가 여행 기간 안에서 어떤 위치인지 표시하기 위한 타입
enum class RoomRangeType {
    NONE,        // 여행 없음
    SINGLE,      // 1일짜리 여행
    START,       // 기간의 시작일
    MIDDLE,      // 기간 사이 날짜
    END          // 기간의 마지막 날
}

// 원래 CalendarDay_rm 에 필드 하나 추가
data class CalendarDay_rm(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val roomRangeType: RoomRangeType = RoomRangeType.NONE
)
