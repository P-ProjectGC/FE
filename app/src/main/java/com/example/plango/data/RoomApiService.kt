package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.ChatMessageDto
import com.example.plango.model.CreateRoomRequest
import com.example.plango.model.CreateWishlistPlaceRequest
import com.example.plango.model.RoomDto
import com.example.plango.model.WishlistPlaceDto
import com.example.plango.model.CreateScheduleRequest
import com.example.plango.model.DelegateHostRequest
import com.example.plango.model.RoomDetailResponse
import com.example.plango.model.ScheduleDto
import com.example.plango.model.UpdateScheduleRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomApiService {

    // 여행방 생성 (로그인된 사용자 기준으로 방 생성)
    @POST("/api/rooms")
    suspend fun createRoom(
        @Body request: CreateRoomRequest
    ): Response<ApiResponse<RoomDto>>

    // 내 여행방 목록 조회 (로그인된 사용자 기준)
    @GET("/api/rooms")
    suspend fun getRooms(
        @Query("keyword") keyword: String? = null
    ): Response<ApiResponse<List<RoomDto>>>

    // 위시리스트 장소 추가
    @POST("/api/rooms/{roomId}/places")
    suspend fun createWishlistPlace(
        @Path("roomId") roomId: Long,
        @Body request: CreateWishlistPlaceRequest
    ): Response<ApiResponse<WishlistPlaceDto>>

    // 위시리스트 조회
    @GET("/api/rooms/{roomId}/places")
    suspend fun getWishlistPlaces(
        @Path("roomId") roomId: Long
    ): Response<ApiResponse<List<WishlistPlaceDto>>>

    // 위시리스트 삭제
    @DELETE("/api/rooms/{roomId}/places/{placeId}")
    suspend fun deleteWishlistPlace(
        @Path("roomId") roomId: Long,
        @Path("placeId") placeId: Long
    ): Response<ApiResponse<RoomDto>>

    // 일정 생성
    @POST("/api/rooms/{roomId}/schedules")
    suspend fun createSchedule(
        @Path("roomId") roomId: Long,
        @Body request: CreateScheduleRequest
    ): Response<ApiResponse<ScheduleDto>>

    // 일정 조회
    @GET("/api/rooms/{roomId}/schedules")
    suspend fun getSchedules(
        @Path("roomId") roomId: Long,
        @Query("dayIndex") dayIndex: Int
    ): Response<ApiResponse<List<ScheduleDto>>>

    // 일정 수정
    @PATCH("/api/rooms/{roomId}/schedules/{scheduleId}")
    suspend fun updateSchedule(
        @Path("roomId") roomId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("memo") memo: String? = null
    ): Response<ApiResponse<Unit>>

    // 일정 삭제
    @DELETE("/api/rooms/{roomId}/schedules/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("roomId") roomId: Long,
        @Path("scheduleId") scheduleId: Long
    ): Response<ApiResponse<Unit>>

    // ✅ 방 상세조회 API
    @GET("api/rooms/{roomId}")
    suspend fun getRoomDetail(
        @Path("roomId") roomId: Long
    ): RoomDetailResponse

    //방장위임
    @PATCH("api/rooms/{roomId}/host")
    suspend fun delegateHost(
        @Path("roomId") roomId: Long,
        @Body request: DelegateHostRequest
    ): Response<ApiResponse<Unit>>




}
