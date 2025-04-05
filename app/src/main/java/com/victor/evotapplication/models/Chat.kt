package com.victor.evotapplication.models

data class Chat(
    val senderId: String = "",
    val senderName: String = "",
    val text: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "image" / "video"
    val timestamp: Long = System.currentTimeMillis()
)