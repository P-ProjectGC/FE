package com.example.plango.model

data class ScheduleDto(
    val scheduleId: Long,
    val dayIndex: Int,
    val startTime: String,
    val endTime: String,
    val roomPlaceId: Long,
    val memo: String?
)

