package com.example.plango.model

data class ChatMessageDto(
    val messageId: Long,
    val roomId: Long,
    val senderId: Long,
    val senderNickname: String,
    val content: String,
    val sentAt: String    // WebSocket + GET 모두 이 이름 사용
)
