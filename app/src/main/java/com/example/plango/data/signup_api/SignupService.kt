package com.example.plango.data.signup_api

import com.example.plango.model.signup_api.SignupRequest
import com.example.plango.model.signup_api.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SignupService {

    @POST("/api/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>
}