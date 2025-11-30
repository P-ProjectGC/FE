package com.example.plango.data

import com.example.plango.model.ChatContentType
import com.example.plango.model.ChatMessage
import android.net.Uri

object ChatRepository {

    // roomId 별로 메시지 리스트를 들고 있는 맵
    private val roomMessages: MutableMap<Long, MutableList<ChatMessage>> = mutableMapOf()

    // ✅ 방 별 현재 메시지 목록 조회
    fun getMessages(roomId: Long): List<ChatMessage> {
        return roomMessages[roomId] ?: emptyList()
    }

    // ✅ 방에 새 메시지 추가
    fun addMessage(roomId: Long, message: ChatMessage) {
        val list = roomMessages.getOrPut(roomId) { mutableListOf() }
        list.add(message)
    }

    // ✅ 방별 메시지 모두 교체 (나중에 서버에서 전체 로드할 때 쓰기 좋음)
    fun setMessages(roomId: Long, messages: List<ChatMessage>) {
        roomMessages[roomId] = messages.toMutableList()
    }

    // ✅ 특정 방 기록 삭제 (예: 방 나가기 등)
    fun clearRoom(roomId: Long) {
        roomMessages.remove(roomId)
    }

    // ✅ 전체 초기화 (테스트용)
    fun clearAll() {
        roomMessages.clear()
    }

    // 🔹 테스트용: 1번 방에만 기본 더미 채팅 넣어두고 싶으면 이런 식으로도 가능
    init {
        val demoRoomId = 1L
        val demoList = mutableListOf<ChatMessage>()

        demoList.add(
            ChatMessage(
                id = 1L,
                senderName = "금연호소인",
                message = "안녕하세요! 여행 기대되네요 😄",
                timeText = "10:23",
                isMe = false,
                imageUri = null,
                type = ChatContentType.TEXT
            )
        )
        demoList.add(
            ChatMessage(
                id = 2L,
                senderName = "로또누나",
                message = "저도요! 날씨 좋았으면 좋겠어요.",
                timeText = "10:25",
                isMe = false,
                imageUri = null,
                type = ChatContentType.TEXT
            )
        )
        demoList.add(
            ChatMessage(
                id = 3L,
                senderName = "나",
                message = "해운대 꼭 가보고 싶었어요!",
                timeText = "10:27",
                isMe = true,
                imageUri = null,
                type = ChatContentType.TEXT
            )
        )

        roomMessages[demoRoomId] = demoList
    }
}
