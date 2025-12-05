package com.example.plango.data

import android.util.Log
import com.example.plango.model.RoomDetailData
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
     * ✅ RoomScheduleTestActivity에서 상세조회로 받은 멤버 정보를
     *    Repository에 반영해서 방 목록 카드도 최신 상태로 맞춰준다.
     */
    fun updateRoomMembersFromDetail(roomId: Long, memberNicknames: List<String>) {
        val index = rooms.indexOfFirst { it.id == roomId }
        if (index == -1) {
            Log.w("TravelRoomRepository", "updateRoomMembersFromDetail: room not found (id=$roomId)")
            return
        }

        val old = rooms[index]
        val newCount = if (memberNicknames.isNotEmpty()) memberNicknames.size else old.memberCount

        val updated = old.copy(
            memberNicknames = memberNicknames,
            memberCount = newCount
        )

        rooms[index] = updated
        Log.d(
            "TravelRoomRepository",
            "updateRoomMembersFromDetail: id=$roomId, members=$memberNicknames, count=$newCount"
        )
    }





    /**
     * 서버에서 여행방 목록을 가져와 rooms 리스트를 갱신
     *
     * @param keyword    메모/제목 검색용 키워드(없으면 null)
     *
     * @return true  -> 서버에서 목록을 정상적으로 가져옴
     *         false -> 실패(HTTP 에러, 예외, code != 0 등)
     */
    suspend fun fetchRoomsFromServer(
        keyword: String? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 🔹 목록 API (이건 Response<...> 형태라고 가정 – 기존 코드 그대로)
                val response = RetrofitClient.roomApiService.getRooms(keyword)
                Log.d("TravelRoomRepository", "getRooms response = $response")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("TravelRoomRepository", "getRooms body = $body")

                    if (body?.code == 0) {
                        val dtoList: List<RoomDto> = body.data ?: emptyList()

                        rooms.clear()

                        // 1차: 목록 DTO → TravelRoom (기본 정보만)
                        val baseRooms = dtoList.map { mapDtoToTravelRoom(it) }

                        // ✅ 2차: roomId 기준으로 중복 제거
                        val distinctBaseRooms = baseRooms.distinctBy { it.id }

                        // 3차: 각 방에 대해 상세조회로 멤버/방장 정보 보정
                        for (base in distinctBaseRooms) {
                            val enriched = enrichRoomWithDetail(base)
                            rooms.add(enriched)
                        }

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
     * (목록에서 오는 "기본 정보"만 사용)
     */
    private fun mapDtoToTravelRoom(dto: RoomDto): TravelRoom {
        // 목록에서는 members 가 안 올 수도 있으니, 여기서는 기본 정보만 세팅
        return TravelRoom(
            id = dto.roomId,
            title = dto.roomName,
            startDate = dto.startDate,
            endDate = dto.endDate,
            dateText = "${dto.startDate} - ${dto.endDate}",
            memo = dto.memo,
            memberCount = 1,               // 임시값 → 상세조회에서 보정
            memberNicknames = emptyList(), // 임시값 → 상세조회에서 보정
            isHost = dto.host == true      // 목록에서도 host가 오면 일단 반영
        )
    }

    /**
     * ✅ 상세조회 API를 이용해 방 정보를 보강
     * - RoomDetailData 기준으로 memberNicknames / memberCount / isHost 등을 덮어씀
     */
    private suspend fun enrichRoomWithDetail(base: TravelRoom): TravelRoom {
        return withContext(Dispatchers.IO) {
            try {
                // 🔹 여기서는 Response<T> 가 아니라 RoomDetailResponse 를 바로 받는다고 가정
                val detailResponse = RetrofitClient.roomApiService.getRoomDetail(base.id)
                val detail: RoomDetailData? = detailResponse.data

                if (detailResponse.code == "0" && detail != null) {
                    // 참여자 닉네임 리스트
                    val memberNicknames: List<String> =
                        detail.members.map { it.nickname }

                    // 인원 수 (멤버 리스트 크기)
                    val memberCount: Int =
                        memberNicknames.size.takeIf { it > 0 } ?: base.memberCount

                    // host 는 Boolean
                    val isHost = detail.host

                    return@withContext base.copy(
                        title = detail.roomName,
                        startDate = detail.startDate,
                        endDate = detail.endDate,
                        memo = detail.memo,
                        memberNicknames = memberNicknames,
                        memberCount = memberCount,
                        isHost = isHost
                    )
                }
            } catch (e: Exception) {
                Log.e("TravelRoomRepository", "getRoomDetail exception", e)
            }

            // 실패하면 그냥 원래 값 그대로 반환
            base
        }
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
