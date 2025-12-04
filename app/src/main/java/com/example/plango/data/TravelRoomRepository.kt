package com.example.plango.data

import android.util.Log
import com.example.plango.model.RoomDto
import com.example.plango.model.TravelRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TravelRoomRepository {

    // 서버에서 받아온 방 목록이 여기에 들어감
    private val rooms = mutableListOf<TravelRoom>()

    /**
     * 현재 메모리에 올라와 있는 여행방 리스트 반환
     * (RoomFragment, RoomSearchDialogFragment 등에서 사용)
     */
    fun getRooms(): List<TravelRoom> = rooms

    fun clearRooms() {
        rooms.clear()
    }

    /**
     * 로컬에서 새로 만든 방(방 생성 직후) 추가
     * - 보통 맨 앞에 끼워 넣어서 최신 방이 위로 오게 함
     */
    fun addRoom(room: TravelRoom) {
        rooms.add(0, room)
    }

    /**
     * roomId로 방 하나 찾기
     * - RoomScheduleTestActivity 에서 ROOM_ID 로 찾을 때 사용
     */
    fun getRoomById(id: Long): TravelRoom? {
        return rooms.find { it.id == id }
    }

    /**
     * 서버에서 여행방 목록을 가져와 rooms 리스트를 갱신
     *
     * @param memberId   X-MEMBER-ID 헤더에 들어갈 현재 사용자 ID
     * @param keyword    메모 검색용 키워드(없으면 null)
     *
     * @return true  -> 서버에서 목록을 정상적으로 가져옴
     *         false -> 실패(HTTP 에러, 예외, code != 0 등)
     */
    suspend fun fetchRoomsFromServer(
        keyword: String? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 🔥 JWT 토큰 기반이라 memberId 안 넘김
                val response = RetrofitClient.roomApiService.getRooms(keyword)
                Log.d("TravelRoomRepository", "getRooms response = $response")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("TravelRoomRepository", "getRooms body = $body")

                    if (body?.code == 0) {
                        val dtoList: List<RoomDto> = body.data ?: emptyList()

                        rooms.clear()
                        rooms.addAll(dtoList.map { mapDtoToTravelRoom(it) })

                        return@withContext true
                    } else {
                        Log.w(
                            "TravelRoomRepository",
                            "getRooms api fail code=${body?.code}, msg=${body?.message}"
                        )
                    }
                } else {
                    Log.w(
                        "TravelRoomRepository",
                        "getRooms http fail code=${response.code()}"
                    )
                }

                // 여기로 오면 실패로 처리
                rooms.clear()
                rooms.add(defaultDummyRoom())
                false
            } catch (e: Exception) {
                Log.e("TravelRoomRepository", "getRooms exception", e)
                rooms.clear()
                rooms.add(defaultDummyRoom())
                false
            }
        }
    }


    /**
     * 서버 RoomDto -> 앱에서 쓰는 TravelRoom 으로 변환
     * - host: 이 X-MEMBER-ID 기준으로 방장인지 여부
     * - members: 상세조회/목록에 따라 null 일 수도 있음
     */
    private fun mapDtoToTravelRoom(dto: RoomDto): TravelRoom {
        val memberNicknames = dto.members?.map { it.nickname } ?: emptyList()
        val memberCount = dto.members?.size ?: 1

        return TravelRoom(
            id = dto.roomId,
            title = dto.roomName,
            startDate = dto.startDate,
            endDate = dto.endDate,
            dateText = "${dto.startDate} - ${dto.endDate}",
            memo = dto.memo,
            memberCount = memberCount,
            memberNicknames = memberNicknames,
            isHost = dto.host == true      // 서버가 내려준 host 플래그 그대로 사용
        )
    }

    /**
     * 서버 호출 실패 시 보여줄 더미 방 하나
     * - 완전 장애 상황에서 화면이 완전 텅 비지 않게 하기 위한 용도
     */
    private fun defaultDummyRoom(): TravelRoom {
        return TravelRoom(
            id = -1L,
            title = "서버 연결 실패",
            startDate = "2025-01-01",
            endDate = "2025-01-02",
            dateText = "서버에서 여행방 목록을 불러오지 못했어요",
            memo = "네트워크 상태를 확인해 주세요.",
            memberCount = 1,
            memberNicknames = listOf("ME"),
            isHost = false
        )
    }
}
