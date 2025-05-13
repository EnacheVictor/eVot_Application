package com.victor.evotapplication.models

data class Listing(
    val id: String = "",
    val associationId: String = "",
    val ownerId: String = "",
    val ownerUsername: String = "",
    val contactInfo: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val type: ListingType = ListingType.RENT,
    val imageUrl: String = "",
    val createdAt: Long = 0L
)

enum class ListingType(val displayName: String) {
    RENT("De închiriat"),
    SELL("De vânzare");

    override fun toString(): String = displayName
}
