package com.example.plango.model

data class ChatMessageDto(
    val messageId: Long,
    val roomId: Long,
    val senderId: Long,
    val senderNickname: String,
    val content: String,
    val createdAt: String
)
