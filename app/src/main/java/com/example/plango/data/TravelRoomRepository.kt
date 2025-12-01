package com.example.plango.data

import com.example.plango.model.TravelRoom

object TravelRoomRepository {

    private val rooms = mutableListOf(
        TravelRoom(
            id = 1L,
            title = "여름 부산 여행",
            startDate = "2025-08-03",
            endDate = "2025-08-05",
            dateText = "8월 3일 - 8월 5일",
            memo = "해운대 · 광안리 위주",
            memberCount = 4,
            memberNicknames = listOf("나", "금연호소인", "로또누나", "음주헌터")   // ⭐ 추가
        ),
        TravelRoom(
            id = 2L,
            title = "가을 제주도",
            startDate = "2025-10-10",
            endDate = "2025-10-13",
            dateText = "10월 10일 - 10월 13일",
            memo = "우도, 성산일출봉 예정",
            memberCount = 3,
            memberNicknames = listOf("나", "로또누나", "음주헌터")   // ⭐ 추가
        ),
        TravelRoom(
            id = 3L,
            title = "도쿄 먹방 여행",
            startDate = "2025-12-01",
            endDate = "2025-12-04",
            dateText = "12월 1일 - 12월 4일",
            memo = "스시 + 라멘 투어",
            memberCount = 2,
            memberNicknames = listOf("나", "금연호소인")   // ⭐ 추가
        )
    )

    fun getRooms(): List<TravelRoom> = rooms

    fun clearRooms() {
        rooms.clear()
    }

    fun addRoom(room: TravelRoom) {
        rooms.add(0, room)
    }

    fun getRoomById(id: Long): TravelRoom? {
        return rooms.find { it.id == id }
    }

}
