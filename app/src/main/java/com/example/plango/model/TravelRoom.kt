package com.example.plango.model

data class TravelRoom(
    val id: Long,
    val title: String,
    val startDate: String,
    val endDate: String,
    val dateText: String,
    val memo: String,
    val memberCount: Int,
    val memberNicknames: List<String> = emptyList(),
    // 🔽 방장 ID (지금은 기기 기준, 나중에 userId로 교체 예정)
    val hostId: String = ""   // 기본값 ""로 해서 기존 더미 데이터도 안전하게
)

