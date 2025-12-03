package com.example.plango.data

import com.example.plango.model.AcceptedFriendship
import com.example.plango.model.ApiResponse
import com.example.plango.model.CreatedFriendRequest //
import com.example.plango.model.FriendRequest
import com.example.plango.model.FriendRequestResponse
import com.example.plango.model.FriendResponse
import com.example.plango.model.SentFriendRequestItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendApiService {
    @POST("/api/friends")
    suspend fun sendFriendRequest(
        @Header("X-Member-Id") memberId: Long,
        @Body request: FriendRequest
    ): Response<ApiResponse<CreatedFriendRequest>> //


    @POST("/api/friends/{friendId}/accept")
    suspend fun acceptFriendRequest(
        @Header("X-Member-Id") memberId: Long,
        @Path("friendId") friendId: Long
    ): Response<ApiResponse<AcceptedFriendship>>

    // 🟢 친구 요청 거절 API 추가
    @POST("/api/friends/{friendId}/reject")
    suspend fun rejectFriendRequest(
        @Header("X-Member-Id") memberId: Long, // 요청을 거절하는 회원 ID
        @Path("friendId") friendId: Long       // 거절할 친구 요청 ID
    ): Response<ApiResponse<Void>> // Void 또는 null을 반환하는 ApiResponse

    @GET("/api/friends")
    suspend fun getFriendList(
        @Header("X-Member-Id") memberId: Long,
        @Query("nickname") nickname: String? = null
    ): Response<ApiResponse<List<FriendResponse>>>

    //친구 요청 조회
    @GET("/api/friends/requests/received")
    suspend fun getReceivedFriendRequests(
        @Header("X-Member-Id") memberId: Long
    ): Response<ApiResponse<List<FriendRequestResponse>>>

    @GET("/api/members/search")
    suspend fun searchMember(
        @Header("X-Member-Id") memberId: Long,
        @Query("nickname") nickname: String
    ): MemberSearchResponse

    @GET("/api/friends/requests/sent")
    suspend fun getSentFriendRequests(
        @Header("X-Member-Id") memberId: Long
    ): Response<ApiResponse<List<SentFriendRequestItem>>>

    @POST("/api/friends/{friendId}/cancel")
    suspend fun cancelFriendRequest(
        @Header("X-Member-Id") memberId: Long,
        @Path("friendId") friendId: Long
    ): Response<ApiResponse<Void>>


}