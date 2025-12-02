package com.example.plango.model

data class RoomDto(
    val roomId: Long,
    val roomName: String,
    val memo: String?,
    val startDate: String,
    val endDate: String,
    val ownerId: Long,
    val memberIds: List<Long>
)
