// WishlistPlaceDto.kt
package com.example.plango.model

data class WishlistPlaceDto(
    val placeId: Long,
    val name: String,
    val address: String,
    val createdByMemberId: Long
)
