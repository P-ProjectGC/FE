package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.ChatMessageDto
import com.example.plango.model.ChatMessageSendRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {

    @POST("/api/rooms/{roomId}/chats")
    suspend fun sendChat(
        @Path("roomId") roomId: Long,
        @Body request: ChatMessageSendRequest
    ): Response<ApiResponse<ChatMessageDto>>   // ← 여기 ApiResponse<T> 는 네가 이미 쓰고 있는 공통 래퍼 타입 이름으로 맞춰줘


    @GET("/api/rooms/{roomId}/chats")
    suspend fun getRecentChats(
        @Path("roomId") roomId: Long
    ): Response<ApiResponse<List<ChatMessageDto>>>


    @GET("/api/rooms/{roomId}/chats/history")
    suspend fun getChatHistory(
        @Path("roomId") roomId: Long,
        @Query("beforeMessageId") beforeMessageId: Long,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<List<ChatMessageDto>>>


}
