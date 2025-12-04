package com.example.plango.data

import com.example.plango.model.ApiResponse
import com.example.plango.model.MemberProfileResponse
import com.example.plango.model.ProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface MemberService {
    @GET("/api/members/{memberId}")
    suspend fun getMemberProfile(
        @Path("memberId") memberId: Long
    ): Response<MemberProfileResponse>

    //프로필 수정
    @PATCH("/api/members/{memberId}")
    suspend fun updateProfile(
        @Path("memberId") memberId: Long,
        @Body request: ProfileUpdateRequest
    ): Response<ApiResponse<Any>>



}
