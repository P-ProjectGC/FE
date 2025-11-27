package com.example.plango.data

import com.example.plango.model.TravelRoom

object TravelRoomRepository {

    // 지금은 더미 데이터. 나중에 API 붙이면 이 부분만 바꾸면 됨.
    fun getRooms(): List<TravelRoom> {
        return listOf(
            TravelRoom(
                id = 1L,
                title = "여름 부산 여행",
                dateText = "10월 28일 - 10월 30일",
                memo = "해운대 방문 예정",
                memberCount = 4
            ),
            TravelRoom(
                id = 2L,
                title = "제주도 힐링",
                dateText = "11월 5일 - 11월 7일",
                memo = "렌터카 예약 완료",
                memberCount = 2
            ),
            TravelRoom(
                id = 3L,
                title = "가을 단풍 여행",
                dateText = "11월 15일 - 11월 17일",
                memo = "케이블카 예약 필요",
                memberCount = 6
            ),
            TravelRoom(
                id = 4L,
                title = "겨울 강릉 여행",
                dateText = "12월 10일 - 12월 12일",
                memo = "팬티투어 by 신진성",
                memberCount = 3
            )
        )
    }

    // 빈 화면이 잘 나오는지 테스트하고 싶을 때
    fun getEmptyRooms(): List<TravelRoom> = emptyList()
}
