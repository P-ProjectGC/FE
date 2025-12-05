package com.example.plango.data

import com.example.plango.model.ChatContentType
import com.example.plango.model.ChatMessage
import com.example.plango.model.ChatMessageDto
import com.example.plango.model.RoomMemberDetail      // 🔹 추가
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ChatRepository {

    private val roomMessages: MutableMap<Long, MutableList<ChatMessage>> = mutableMapOf()

    // 🔹 방별 (memberId -> profileImageUrl) 캐시
    private val memberProfileMapByRoom: MutableMap<Long, Map<Long, String?>> = mutableMapOf()

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
        memberProfileMapByRoom.clear()
    }

    // ─────────────────────────────────────────────
    // 🔹 방 멤버 → 프로필 URL 매핑 세팅
    //    (RoomDetailData.members 를 그대로 넘겨서 사용)
    // ─────────────────────────────────────────────
    fun setMemberProfiles(roomId: Long, members: List<RoomMemberDetail>) {
        val map = members.associate { m ->
            m.memberId to m.profileImageUrl
        }
        memberProfileMapByRoom[roomId] = map
    }

    private fun getProfileUrl(roomId: Long, memberId: Long?): String? {
        if (memberId == null) return null
        val roomMap = memberProfileMapByRoom[roomId] ?: return null
        return roomMap[memberId]
    }

    /**
     * STOMP/WebSocket으로 들어온 메시지를 반영할 때 사용
     */
    fun addIncomingMessageFromServer(
        roomId: Long,
        dto: ChatMessageDto,
        currentMemberId: Long?
    ): ChatMessage {
        // 🔹 roomId 도 같이 넘기도록 변경
        val chatMessage = dto.toDomain(roomId, currentMemberId)
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
        val list = dtos.map { it.toDomain(roomId, currentMemberId) }
        setMessages(roomId, list)
    }

    // 🔹 roomId 를 같이 받도록 변경
    private fun ChatMessageDto.toDomain(
        roomId: Long,
        currentMemberId: Long?
    ): ChatMessage {
        val isMe = currentMemberId != null && senderId == currentMemberId

        val timeText = parseServerTime(sentAt)?.format(displayTimeFormatter)
            ?: LocalDateTime.now().format(displayTimeFormatter)

        // 🔹 senderId 기준으로 프로필 URL 찾아오기
        val profileUrl = getProfileUrl(roomId, senderId)

        // content 가 URL이면 나중에 IMAGE 타입으로도 바꿀 수 있음
        return ChatMessage(
            id = messageId,
            senderName = senderNickname,
            message = content,
            timeText = timeText,
            isMe = isMe,
            imageUri = null,
            type = ChatContentType.TEXT,
            profileImageUrl = profileUrl              // ← 여기!
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

        // 🔹 여기서도 roomId 넘겨줌
        val newMessages = dtos.map { it.toDomain(roomId, currentMemberId) }

        // 과거 → 현재 순서로 왼쪽(앞)에 붙이는 형태
        val merged = newMessages + existing
        roomMessages[roomId] = merged.toMutableList()
    }
}
