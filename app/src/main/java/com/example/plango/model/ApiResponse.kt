package com.example.plango.model

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)