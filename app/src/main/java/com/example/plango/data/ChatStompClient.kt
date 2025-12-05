package com.example.plango.data

import android.content.Context
import android.util.Log
import com.example.plango.data.token.TokenManager
import com.example.plango.model.ChatMessageDto
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader

object ChatStompClient {

    private const val TAG = "ChatStompClient"

    private var stompClient: StompClient? = null
    private var roomSubscription: Disposable? = null

    private val gson = Gson()

    // ⚠️ 여기 중요한 포인트: TokenManager는 인스턴스라서 이렇게 들고 있어야 함
    private lateinit var tokenManager: TokenManager

    /**
     * 반드시 앱 시작 시점(예: Application.onCreate)이나,
     * 로그인 완료 시점에 한 번 호출해서 TokenManager를 넣어줘야 한다.
     */
    fun init(context: Context) {
        // applicationContext 써서 메모리릭 방지
        tokenManager = TokenManager(context.applicationContext)
    }

    private fun ensureInitialized() {
        if (!::tokenManager.isInitialized) {
            throw IllegalStateException(
                "ChatStompClient not initialized. " +
                        "Call ChatStompClient.init(context) before using it."
            )
        }
    }

    // WebSocket URL: ws(s)://{backend}/ws/chat
    private fun buildWebSocketUrl(): String {
        // RetrofitClient.BASE_URL 이 "https://backend..." 라고 가정
        val base = RetrofitClient.BASE_URL.trimEnd('/')
        val wsBase = base.replaceFirst("http", "ws")
        return "$wsBase/ws/chat"
    }

    /**
     * 필요할 때만 연결. 이미 연결되어 있으면 재사용.
     */
    fun connectIfNeeded() {
        ensureInitialized()

        if (stompClient?.isConnected == true) return

        val url = buildWebSocketUrl()
        Log.d(TAG, "Connecting to STOMP: $url")

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

        // 연결 상태 로그
        stompClient?.lifecycle()
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({ event ->
                Log.d(TAG, "STOMP event: ${event.type}")
            }, { error ->
                Log.e(TAG, "STOMP lifecycle error", error)
            })

        // Authorization 헤더 포함해서 연결
        val headers = buildConnectHeaders()
        stompClient?.connect(headers)
    }

    private fun buildConnectHeaders(): List<StompHeader> {
        ensureInitialized()

        val headers = mutableListOf<StompHeader>()
        val token = tokenManager.getAccessToken()

        if (!token.isNullOrBlank()) {
            headers.add(StompHeader("Authorization", "Bearer $token"))
        }

        return headers
    }

    /**
     * 특정 roomId 채널 구독
     * - 메시지 도착 시 onMessage(dto) 콜백 호출
     */
    fun subscribeRoom(
        roomId: Long,
        onMessage: (ChatMessageDto) -> Unit
    ) {
        connectIfNeeded()

        // 이전 구독 있으면 정리
        roomSubscription?.dispose()
        val destination = "/topic/rooms/$roomId"

        roomSubscription = stompClient
            ?.topic(destination)
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({ stompMessage ->
                try {
                    val payload = stompMessage.payload
                    val dto = gson.fromJson(payload, ChatMessageDto::class.java)
                    onMessage(dto)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse STOMP message", e)
                }
            }, { error ->
                Log.e(TAG, "STOMP subscribe error", error)
            })

        Log.d(TAG, "Subscribed to $destination")
    }

    fun unsubscribeRoom() {
        roomSubscription?.dispose()
        roomSubscription = null
    }

    fun disconnect() {
        unsubscribeRoom()
        stompClient?.disconnect()
        stompClient = null
    }


    fun sendChatMessage(roomId: Long, memberId: Long, content: String) {
        val json = gson.toJson(
            mapOf(
                "memberId" to memberId,
                "content" to content
            )
        )

        stompClient?.send("/app/rooms/$roomId", json)
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({
                Log.d(TAG, "STOMP send 성공: $json")
            }, { error ->
                Log.e(TAG, "STOMP send 실패", error)
            })
    }

}
