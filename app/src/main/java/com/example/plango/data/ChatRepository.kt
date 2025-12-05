package com.example.plango.data

import com.example.plango.model.ChatContentType
import com.example.plango.model.ChatMessage
import com.example.plango.model.ChatMessageDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ChatRepository {

    private val roomMessages: MutableMap<Long, MutableList<ChatMessage>> = mutableMapOf()

    // 서버 포맷: "2025-12-05T14:21:33" (ms 없을 수도 있음)
    private val serverFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    )

    private val displayTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    fun getMessages(roomId: Long): List<ChatMessage> =
        roomMessages[roomId] ?: emptyList()

    fun addMessage(roomId: Long, message: ChatMessage) {
        val list = roomMessages.getOrPut(roomId) { mutableListOf() }
        list.add(message)
    }

    fun setMessages(roomId: Long, messages: List<ChatMessage>) {
        roomMessages[roomId] = messages.toMutableList()
    }

    fun clearRoom(roomId: Long) {
        roomMessages.remove(roomId)
    }

    fun clearAll() {
        roomMessages.clear()
    }

    /**
     * STOMP/WebSocket으로 들어온 메시지를 반영할 때 사용
     */
    fun addIncomingMessageFromServer(
        roomId: Long,
        dto: ChatMessageDto,
        currentMemberId: Long?
    ): ChatMessage {
        val chatMessage = dto.toDomain(currentMemberId)
        addMessage(roomId, chatMessage)
        return chatMessage
    }

    // HTTP GET /chats, /chats/history 응답 data(List<ChatMessageDto>)를
    // 한 번에 세팅할 때도 재사용 가능
    fun setMessagesFromDtos(
        roomId: Long,
        dtos: List<ChatMessageDto>,
        currentMemberId: Long?
    ) {
        val list = dtos.map { it.toDomain(currentMemberId) }
        setMessages(roomId, list)
    }

    private fun ChatMessageDto.toDomain(currentMemberId: Long?): ChatMessage {
        val isMe = currentMemberId != null && senderId == currentMemberId

        val timeText = parseServerTime(sentAt)?.format(displayTimeFormatter)
            ?: LocalDateTime.now().format(displayTimeFormatter)

        // content 가 URL이면 나중에 IMAGE 타입으로도 바꿀 수 있음
        return ChatMessage(
            id = messageId,
            senderName = senderNickname,
            message = content,
            timeText = timeText,
            isMe = isMe,
            imageUri = null,
            type = ChatContentType.TEXT
        )
    }

    private fun parseServerTime(value: String): LocalDateTime? {
        for (fmt in serverFormats) {
            try {
                return LocalDateTime.parse(value, fmt)
            } catch (_: Exception) {
            }
        }
        return null
    }

    // ChatRepository.kt 안에 추가

    /**
     * /chats/history 응답을 기존 목록 앞에 붙일 때 사용
     */
    fun prependMessagesFromDtos(
        roomId: Long,
        dtos: List<ChatMessageDto>,
        currentMemberId: Long?
    ) {
        if (dtos.isEmpty()) return

        val existing = roomMessages[roomId] ?: mutableListOf()

        // 서버에서 내려온 과거 메시지들을 도메인 모델로 변환
        val newMessages = dtos.map { it.toDomain(currentMemberId) }

        // 과거 → 현재 순서로 왼쪽(앞)에 붙이는 형태
        val merged = newMessages + existing
        roomMessages[roomId] = merged.toMutableList()
    }


}
