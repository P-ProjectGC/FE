package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.CreateRoomRequest
import com.example.plango.model.CreateWishlistPlaceRequest
import com.example.plango.model.RoomDto
import com.example.plango.model.WishlistPlaceDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.plango.model.CreateScheduleRequest
import com.example.plango.model.ScheduleDto
import com.example.plango.model.UpdateScheduleRequest
import retrofit2.http.PATCH

interface RoomApiService {


    //여행방 생성 Post,Get
    @POST("/api/rooms")
    suspend fun createRoom(
        @Header("X-MEMBER-ID") memberId: Long,
        @Body request: CreateRoomRequest
    ): Response<ApiResponse<RoomDto>>


    @GET("/api/rooms")
    suspend fun getRooms(
        @Header("X-MEMBER-ID") memberId: Long,
        @Query("keyword") keyword: String? = null
    ): Response<ApiResponse<List<RoomDto>>>

    //위시리스트 Post, Get, DELETE
    @POST("/api/rooms/{roomId}/places")
    suspend fun createWishlistPlace(
        @Path("roomId") roomId: Long,
        @Header("X-MEMBER-ID") memberId: Long,
        @Body request: CreateWishlistPlaceRequest
    ): Response<ApiResponse<WishlistPlaceDto>>


    @GET("/api/rooms/{roomId}/places")
    suspend fun getWishlistPlaces(
        @Path("roomId") roomId: Long
    ): Response<ApiResponse<List<WishlistPlaceDto>>>

    @DELETE("/api/rooms/{roomId}/places/{placeId}")
    suspend fun deleteWishlistPlace(
        @Path("roomId") roomId: Long,
        @Path("placeId") placeId: Long
    ): Response<ApiResponse<RoomDto>>




   //일정 생성,수정,삭제
    @POST("/api/rooms/{roomId}/schedules")
    suspend fun createSchedule(
        @Path("roomId") roomId: Long,
        @Header("X-MEMBER-ID") memberId: Long,
        @Body request: CreateScheduleRequest
    ): Response<ApiResponse<ScheduleDto>>


    @GET("/api/rooms/{roomId}/schedules")
    suspend fun getSchedules(
        @Path("roomId") roomId: Long,
        @Query("dayIndex") dayIndex: Int
    ): Response<ApiResponse<List<ScheduleDto>>>

    // RoomApiService.kt 인터페이스 (수정)
    // RoomApiService.kt 인터페이스 파일
    @PATCH("api/rooms/{roomId}/schedules/{scheduleId}")
    suspend fun updateSchedule(
        @Path("roomId") roomId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Header("X-MEMBER-ID") memberId: Long,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("memo") memo: String? = null // 서버 테스트에 memo가 포함되므로 Nullable로 포함
    ): Response<ApiResponse<Unit>>

    // 🚨 [추가] 2. 일정 삭제 (DELETE) API 정의 (빨간 줄 해결)
    @DELETE("/api/rooms/{roomId}/schedules/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("roomId") roomId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Header("X-MEMBER-ID") memberId: Long
    ): Response<ApiResponse<Unit>> // 반환 데이터가 없으므로 Unit 사용








}
