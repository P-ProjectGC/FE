package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.ChatMessageDto
import com.example.plango.model.ChatMessageSendRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {

    @POST("/api/rooms/{roomId}/chats")
    suspend fun sendChatMessage(
        @Path("roomId") roomId: Long,
        @Body request: ChatMessageSendRequest
    ): ApiResponse<ChatMessageDto>   // ← 여기 ApiResponse<T> 는 네가 이미 쓰고 있는 공통 래퍼 타입 이름으로 맞춰줘
}
