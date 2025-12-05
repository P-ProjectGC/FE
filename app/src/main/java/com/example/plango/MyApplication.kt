package com.example.plango

import android.app.Application
import com.example.plango.data.RetrofitClient
import com.kakao.sdk.common.KakaoSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 앱 시작 시 1회 Retrofit 초기화
        RetrofitClient.init(this)

        KakaoSdk.init(this, "565207f78fbd8da404f0288d6d5f9ebd")
    }
}