package com.example.plango.data

import android.content.Context
import com.example.plango.data.login_api.AuthService
import com.example.plango.data.token.AuthInterceptor
import com.example.plango.data.token.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 에뮬레이터 → 로컬 스프링 서버 접근
    // 🔁 호스트(네 컴퓨터) 실제 IP + 포트내 ip : 210.119.237.48(진성, 172~~는주희)
    //private const val BASE_URL = "http://172.25.81.234:8080/"


    const val BASE_URL = "https://pyrological-nonsalutarily-hobert.ngrok-free.dev/"
    private lateinit var tokenManager: TokenManager


   

    // 앱 시작 시 1번만 초기화됨 (MyApplication에서 호출)
    fun init(context: Context) {
        tokenManager = TokenManager(context)
    }

    // Gson 설정 (null 허용 / lenient 모드)
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY   // 요청/응답 전체 로그 확인용
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)                  // 로그 출력
            .addInterceptor(AuthInterceptor(tokenManager))       // 🔥 토큰 자동 추가
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // AuthService (로그인/회원가입 등)
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val roomApiService: RoomApiService by lazy {
        retrofit.create(RoomApiService::class.java)
    }

    val friendApiService: FriendApiService by lazy {
        retrofit.create(FriendApiService::class.java)
    }

    val memberApiService: MemberService by lazy {
        retrofit.create(MemberService::class.java)
    }

    val fileApiService: FileService by lazy {
        retrofit.create(FileService::class.java)
    }

}
