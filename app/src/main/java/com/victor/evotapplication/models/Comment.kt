package com.victor.evotapplication.models

import java.util.Date

// Data model representing a comment posted under an announcement

data class Comment(
    val id: String = "",
    val announcementId: String = "",
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null
)