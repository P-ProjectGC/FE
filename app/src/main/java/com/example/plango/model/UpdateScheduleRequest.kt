package com.example.plango.model

/**
 * PATCH /api/rooms/{roomId}/schedules/{scheduleId} 요청의 Request Body
 * (DayIndex는 수정 대상이 아님)
 */
data class UpdateScheduleRequest(
    // 수정할 일정 시작 시간 (HH:mm)
    val startTime: String,
    // 수정할 일정 종료 시간 (HH:mm)
    val endTime: String,

)