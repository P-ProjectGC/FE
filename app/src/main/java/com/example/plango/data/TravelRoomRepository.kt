package com.example.plango.data

import com.example.plango.model.TravelRoom

object TravelRoomRepository {

    // 🔥 실제 데이터가 담기는 mutable 리스트
    private val rooms = mutableListOf(
        TravelRoom(
            id = 1L,
            title = "여름 부산 여행",
            startDate="2025-08-03",
            endDate="2025-08-05",
            dateText = "8월 3일 - 8월 5일",
            memo = "해운대 · 광안리 위주",
            memberCount = 4
        ),
        TravelRoom(
            id = 2L,
            title = "가을 제주도",
            startDate="2025-10-10",
            endDate="2025-10-13",
            dateText = "10월 10일 - 10월 13일",
            memo = "우도, 성산일출봉 예정",
            memberCount = 3
        ),
        TravelRoom(
            id = 3L,
            title = "도쿄 먹방 여행",
            startDate="2025-12-01",
            endDate="2025-12-04",
            dateText = "12월 1일 - 12월 4일",
            memo = "스시 + 라멘 투어",
            memberCount = 2
        )
    )

    // 현재 리스트 반환
    fun getRooms(): List<TravelRoom> = rooms

    // 빈 목록 테스트용 (그냥 참고용으로 두고 싶으면 이렇게)
    fun clearRooms() {
        rooms.clear()
    }

    // 🔥 새 여행방 추가
    fun addRoom(room: TravelRoom) {
        // 새로 만든 방을 맨 위로 올리고 싶으면 add(0, room)
        rooms.add(0, room)
    }
}
