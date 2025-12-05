package com.example.plango.model

import android.net.Uri

// 메시지 종류
enum class ChatContentType {
    TEXT,
    IMAGE
}

data class ChatMessage(
    val id: Long,
    val senderName: String,        // 보낸 사람 닉네임
    val message: String?,          // 텍스트 메시지 (이미지일 땐 null)
    val timeText: String,          // "10:23" 이런 형식
    val isMe: Boolean,             // 내가 보낸 메시지면 true → 오른쪽
    val imageUri: Uri? = null,     // 이미지 메시지일 때만 값 있음
    val type: ChatContentType = ChatContentType.TEXT,

    // 🔹 새로 추가
    val profileImageUrl: String? = null
)
