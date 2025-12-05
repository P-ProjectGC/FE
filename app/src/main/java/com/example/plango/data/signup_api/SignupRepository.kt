package com.example.plango.data.signup_api

import com.example.plango.model.signup_api.SignupRequest
import com.example.plango.model.signup_api.SignupResponse
import retrofit2.Response

class SignupRepository(private val signupService: SignupService) {

    suspend fun signup(request: SignupRequest): Response<SignupResponse> {
        return signupService.signup(request)
    }
}