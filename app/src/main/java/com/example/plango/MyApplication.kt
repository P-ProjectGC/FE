package com.example.plango

import android.app.Application
import com.example.plango.data.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 앱 시작 시 1회 Retrofit 초기화
        RetrofitClient.init(this)
    }
}