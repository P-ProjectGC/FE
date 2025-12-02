// WishlistPlaceDto.kt
package com.example.plango.model

data class WishlistPlaceDto(
    val id: Long,
    val name: String,
    val address: String,
    val formattedAddress: String,
    val googlePlaceId: String,
    val latitude: Double,
    val longitude: Double,
    val createdByMemberId: Long
)
