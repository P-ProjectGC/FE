package com.example.plango.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 에뮬레이터 → 로컬 스프링 서버 접근
    // 🔁 호스트(네 컴퓨터) 실제 IP + 포트
    private const val BASE_URL = "http://172.25.81.234:8080"


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY   // 요청/응답 전체 로그 확인용
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val roomApiService: RoomApiService by lazy {
        retrofit.create(RoomApiService::class.java)
    }
}
