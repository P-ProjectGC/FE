package com.example.plango.model

data class ChatMessage(
    val id: Long,
    val senderName: String,   // 보낸 사람 닉네임
    val message: String,      // 메시지 내용
    val timeText: String,     // "10:23" 이런 형식의 시간 텍스트
    val isMe: Boolean         // 내가 보낸 메시지면 true → 오른쪽 말풍선
)
