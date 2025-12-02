package com.example.plango.model

data class TravelRoom(
    val id: Long,
    val title: String,
    val startDate: String,
    val endDate: String,
    val dateText: String,
    val memo: String?,
    val memberCount: Int,
    val memberNicknames: List<String>,
    val isHost: Boolean = false    // 👈 새로 추가
)


