// CreateWishlistPlaceRequest.kt
package com.example.plango.model



data class CreateWishlistPlaceRequest(
    val name: String,
    val address: String,
    val googlePlaceId: String,
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double
)


