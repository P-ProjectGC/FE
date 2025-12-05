package com.example.plango

import android.app.Application
import com.example.plango.data.RetrofitClient
import com.kakao.sdk.common.KakaoSdk
import com.example.plango.data.ChatStompClient   // ← 추가해야 함

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Retrofit 초기화
        RetrofitClient.init(this)

        KakaoSdk.init(this, "565207f78fbd8da404f0288d6d5f9ebd")
        // WebSocket / STOMP 초기화 (TokenManager 준비)
        ChatStompClient.init(this)   // ← 이 한 줄이 매우 중요!!
    }
}
