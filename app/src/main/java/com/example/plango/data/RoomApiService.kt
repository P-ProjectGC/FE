package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.CreateRoomRequest
import com.example.plango.model.CreateWishlistPlaceRequest
import com.example.plango.model.RoomDto
import com.example.plango.model.WishlistPlaceDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomApiService {

    @POST("/api/rooms")
    suspend fun createRoom(
        @Header("X-MEMBER-ID") memberId: Long,
        @Body request: CreateRoomRequest
    ): Response<ApiResponse<RoomDto>>

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


}
