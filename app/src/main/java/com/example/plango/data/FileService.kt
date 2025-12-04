package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.FileUploadData
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileService {

    @Multipart
    @POST("/api/files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part   // 필드 이름은 "file" 로 가정
    ): Response<ApiResponse<FileUploadData>>
}
