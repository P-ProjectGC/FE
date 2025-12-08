package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.InconvenienceReportData
import com.example.plango.model.InconvenienceReportRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportService {

    // 불편사항 신고
    @POST("/api/v1/reports/inconvenience")
    suspend fun submitInconvenienceReport(
        @Body request: InconvenienceReportRequest
    ): Response<ApiResponse<InconvenienceReportData>>
}
