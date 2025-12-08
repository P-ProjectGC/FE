// com/example/plango/data/NoticeService.kt
package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.Notice
import retrofit2.Response
import retrofit2.http.GET

interface NoticeService {

    @GET("/api/notices")
    suspend fun getNotices(): Response<ApiResponse<List<Notice>>>
}
