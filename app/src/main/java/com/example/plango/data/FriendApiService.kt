package com.example.plango.data

import com.example.plango.model.AcceptedFriendship
import com.example.plango.model.ApiResponse
import com.example.plango.model.CreatedFriendRequest
import com.example.plango.model.FriendRequest
import com.example.plango.model.FriendRequestResponse
import com.example.plango.model.FriendResponse
import com.example.plango.model.SentFriendRequestItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendApiService {

    @POST("/api/friends")
    suspend fun sendFriendRequest(
        @Body request: FriendRequest
    ): Response<ApiResponse<CreatedFriendRequest>>

    @POST("/api/friends/{friendId}/accept")
    suspend fun acceptFriendRequest(
        @Path("friendId") friendId: Long
    ): Response<ApiResponse<AcceptedFriendship>>

    @POST("/api/friends/{friendId}/reject")
    suspend fun rejectFriendRequest(
        @Path("friendId") friendId: Long
    ): Response<ApiResponse<Void>>

    @GET("/api/friends")
    suspend fun getFriendList(
        @Query("nickname") nickname: String? = null
    ): Response<ApiResponse<List<FriendResponse>>>

    // 받은 친구요청 목록
    @GET("/api/friends/requests/received")
    suspend fun getReceivedFriendRequests(): Response<ApiResponse<List<FriendRequestResponse>>>

    // 회원 검색
    @GET("/api/members/search")
    suspend fun searchMember(
        @Query("nickname") nickname: String
    ): MemberSearchResponse

    @GET("/api/friends/requests/sent")
    suspend fun getSentFriendRequests(): Response<ApiResponse<List<SentFriendRequestItem>>>

    @POST("/api/friends/{friendId}/cancel")
    suspend fun cancelFriendRequest(
        @Path("friendId") friendId: Long
    ): Response<ApiResponse<Void>>

    @DELETE("/api/friends/{friendId}")
    suspend fun deleteFriend(
        @Path("friendId") friendId: Long
    ): Response<ApiResponse<Void>>
}
