package com.example.plango

import android.app.Application
import com.example.plango.data.RetrofitClient
import com.example.plango.data.ChatStompClient   // ← 추가해야 함

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Retrofit 초기화
        RetrofitClient.init(this)

        // WebSocket / STOMP 초기화 (TokenManager 준비)
        ChatStompClient.init(this)   // ← 이 한 줄이 매우 중요!!

        // ✅ 알림 채널 생성 (여기서 한 번만 해두면 됨)
        NotificationHelper.createChatNotificationChannel(this)
        NotificationHelper.createFriendRequestNotificationChannel(this)
    }
}
