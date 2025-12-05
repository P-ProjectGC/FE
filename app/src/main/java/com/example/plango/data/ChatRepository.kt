package com.example.plango.data

import com.example.plango.model.ChatContentType
import com.example.plango.model.ChatMessage
import com.example.plango.model.ChatMessageDto
import com.example.plango.model.ChatMessageSendRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatRepository {

    // roomId 별로 메시지 리스트를 들고 있는 맵
    private val roomMessages: MutableMap<Long, MutableList<ChatMessage>> = mutableMapOf()

    // 서버 createdAt 포맷: "2025-01-08T21:45:00.000"
    private val serverTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    // 말풍선에 보여줄 시간 포맷: "21:45"
    private val displayTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    // ✅ 방 별 현재 메시지 목록 조회
    fun getMessages(roomId: Long): List<ChatMessage> {
        return roomMessages[roomId] ?: emptyList()
    }

    // ✅ 방에 새 메시지 추가 (로컬용)
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

    // ✅ 전체 초기화 (테스트/로그아웃 용)
    fun clearAll() {
        roomMessages.clear()
    }

    /**
     * 🔹 텍스트 메시지 전송 API 연동
     *  - POST /api/rooms/{roomId}/chats
     *  - 호출하는 쪽에서 senderMemberId 를 넘겨준다.
     */
    suspend fun sendTextMessageToServer(
        roomId: Long,
        senderMemberId: Long,
        content: String
    ): ChatMessage = withContext(Dispatchers.IO) {

        val request = ChatMessageSendRequest(
            memberId = senderMemberId,
            content = content
        )

        // 🔸 공통 ApiResponse<T> 타입은 프로젝트에 있는 걸 그대로 사용
        val response = RetrofitClient.chatApiService.sendChatMessage(
            roomId = roomId,
            request = request
        )

        if (response.code != 0) {
            throw IllegalStateException("채팅 전송 실패(code=${response.code}): ${response.message}")
        }

        val dto = response.data
            ?: throw IllegalStateException("채팅 전송 실패: ${response.message}")

        val chatMessage = dto.toDomain(currentMemberId = senderMemberId)

        // 로컬 캐시에 추가
        addMessage(roomId, chatMessage)

        chatMessage
    }

    /**
     * 🔹 WebSocket(STOMP) 등으로 서버에서 브로드캐스트된 메시지를
     *     ChatMessageDto로 받았을 때 사용하는 헬퍼.
     *
     * @param roomId         메시지가 도착한 방 ID
     * @param dto            서버에서 내려온 채팅 DTO
     * @param currentMemberId 현재 로그인한 내 memberId (없으면 null 가능)
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

    // ====== 내부 변환 헬퍼 ======

    private fun ChatMessageDto.toDomain(currentMemberId: Long?): ChatMessage {
        val isMe = currentMemberId != null && (senderId == currentMemberId)

        val timeText = try {
            val dateTime = LocalDateTime.parse(this.createdAt, serverTimeFormatter)
            dateTime.format(displayTimeFormatter)
        } catch (e: Exception) {
            // 파싱 실패 시 현재 시간으로 대체
            LocalDateTime.now().format(displayTimeFormatter)
        }

        return ChatMessage(
            id = messageId,
            senderName = senderNickname,
            message = content,
            timeText = timeText,
            isMe = isMe,
            imageUri = null,                 // 현재 API는 TEXT만 → 이미지 X
            type = ChatContentType.TEXT      // 이 엔드포인트는 텍스트 전용
        )
    }


}
