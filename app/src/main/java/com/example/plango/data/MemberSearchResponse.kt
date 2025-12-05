package com.example.plango.data

data class MemberSearchResponse(
    val code: String,
    val message: String,
    val data: List<MemberSearchData>?
)
