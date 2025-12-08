// com/example/plango/data/NoticeRepository.kt
package com.example.plango.data

import android.util.Log
import com.example.plango.model.Notice

class NoticeRepository(
    private val noticeService: NoticeService = RetrofitClient.noticeApiService
) {

    suspend fun fetchNotices(): Result<List<Notice>> {
        return try {
            val response = noticeService.getNotices()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.code == 0 && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "공지사항 조회 실패"))
                }
            } else if (response.code() == 401) {
                Result.failure(Exception("로그인이 필요합니다. (401)"))
            } else {
                Result.failure(Exception("공지사항 조회 중 오류 (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("NoticeRepository", "fetchNotices error", e)
            Result.failure(e)
        }
    }
}
